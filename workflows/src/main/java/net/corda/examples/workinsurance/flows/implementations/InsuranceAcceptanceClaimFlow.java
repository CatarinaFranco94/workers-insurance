package net.corda.examples.workinsurance.flows.implementations;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.flows.models.ClaimInfo;
import net.corda.examples.workinsurance.flows.interfaces.IInsuranceClaimState;
import net.corda.examples.workinsurance.flows.models.InsuranceDetailInfo;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.states.InsuranceDetail;
import net.corda.examples.workinsurance.states.InsuranceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class InsuranceAcceptanceClaimFlow {

    private InsuranceAcceptanceClaimFlow(){}

    @InitiatingFlow
    @StartableByRPC
    public static class InsuranceAcceptanceClaimInitiator extends FlowLogic<SignedTransaction> implements IInsuranceClaimState {

        private final InsuranceDetailInfo insuranceDetailInfo;
        private final String policyNumber;
        private final String claimNumber;

        private final static Logger logger = LoggerFactory.getLogger(InsuranceAcceptanceClaimInitiator.class);

        // TO DO :: Mudar ESTE input - da claimInfo so preciso do ClaimNumber, por isso criar novo obj
        // que tenha o claimNumber e os campos que vao ser necessarios acrescentar para uma acceptance
        public InsuranceAcceptanceClaimInitiator(InsuranceDetailInfo insuranceDetailInfo, String policyNumber, String claimNumber) {
            this.insuranceDetailInfo = insuranceDetailInfo;
            this.policyNumber = policyNumber;
            this.claimNumber = claimNumber;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // Query the vault to fetch a list of all Insurance state, and filter the results based on the policyNumber
            // to fetch the desired Insurance state from the vault. This filtered state would be used as input to the
            // transaction.
            List<StateAndRef<InsuranceState>> insuranceStateAndRefs = getServiceHub().getVaultService()
                    .queryBy(InsuranceState.class).getStates();

            StateAndRef<InsuranceState> inputStateAndRef = insuranceStateAndRefs.stream().filter(insuranceStateAndRef -> {
                InsuranceState insuranceState = insuranceStateAndRef.getState().getData();
                return insuranceState.getWorkerDetail().getPolicyNumber().equals(policyNumber);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Policy Not Found"));

            Claim inputClaim = inputStateAndRef.getState().getData().getClaims().stream().filter(correspondentClaim ->
                correspondentClaim.getClaimNumber().equals(claimNumber) && correspondentClaim.getClaimStatus().equals(this.getPreviousState())
            ).findAny().orElseThrow(() -> new IllegalArgumentException("Proposed Claim Not Found"));

            InsuranceDetail insuranceDetail = new InsuranceDetail(
                    insuranceDetailInfo.getInsuranceCompanyNumber(),
                    insuranceDetailInfo.getInsuranceCompanyPolicyNumber(),
                    insuranceDetailInfo.getField()
            );

            Claim claim = new Claim(claimNumber, inputClaim.getClaimDescription(),
                    inputClaim.getClaimAmount(), this.getNextState(), inputClaim.getInternalPolicyNo(),
                    inputClaim.getAccidentDate(), inputClaim.getEpisodeDate(), inputClaim.getAccidentType(), inputClaim.getModule(), insuranceDetail);

            InsuranceState input = inputStateAndRef.getState().getData();

            List<Claim> claims = new ArrayList<>();
            if(input.getClaims() == null || input.getClaims().size() == 0 ){
                claims.add(claim);
            }else {
                claims.addAll(input.getClaims());
                claims.add(claim);
            }

            //Create the output state
            InsuranceState output = new InsuranceState(input.getInsuredValue(),
                    input.getDuration(), input.getInsurer(), input.getInsuree(),
                    input.getWorkerDetail(), claims);

            // Build the transaction.
            TransactionBuilder transactionBuilder = new TransactionBuilder(inputStateAndRef.getState().getNotary())
                    .addInputState(inputStateAndRef)
                    .addOutputState(output, InsuranceContract.ID)
                    .addCommand(new InsuranceContract.Commands.AcceptClaim(), ImmutableList.of(getOurIdentity().getOwningKey()));

            // Verify the transaction
            transactionBuilder.verify(getServiceHub());

            // Sign the transaction
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // Call finality Flow
            FlowSession counterpartySession = initiateFlow(input.getInsuree());
            return subFlow(new FinalityFlow(signedTransaction, ImmutableList.of(counterpartySession)));
        }

        @Override
        public ClaimStatus getNextState() {
            return ClaimStatus.Accepted;
        }

        @Override
        public ClaimStatus getPreviousState() {
            return ClaimStatus.Proposal;
        }
    }

    @InitiatedBy(InsuranceAcceptanceClaimInitiator.class)
    public static class InsuranceAcceptanceClaimResponder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

        public InsuranceAcceptanceClaimResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }
    }
}
