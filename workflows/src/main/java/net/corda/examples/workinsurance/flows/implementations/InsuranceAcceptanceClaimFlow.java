package net.corda.examples.workinsurance.flows.implementations;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
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

import static java.util.Collections.singletonList;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class InsuranceAcceptanceClaimFlow {

    private InsuranceAcceptanceClaimFlow(){}

    @InitiatingFlow
    @StartableByRPC
    public static class InsuranceAcceptanceClaimInitiator extends FlowLogic<SignedTransaction> implements IInsuranceClaimState {

        private final InsuranceDetailInfo insuranceDetailInfo;
        private final String policyNumber;
        private final String claimNumber;
        private final Party insuree;

        private final static Logger logger = LoggerFactory.getLogger(InsuranceAcceptanceClaimInitiator.class);

        public InsuranceAcceptanceClaimInitiator(InsuranceDetailInfo insuranceDetailInfo, String policyNumber, String claimNumber, Party insuree) {
            this.insuranceDetailInfo = insuranceDetailInfo;
            this.policyNumber = policyNumber;
            this.claimNumber = claimNumber;
            this.insuree = insuree;
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
                return insuranceState.getWorkerDetail().getPolicyNumber().equals(policyNumber) && insuranceState.getInsuree().equals(insuree);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Insuree Policy Not Found"));

            Claim inputClaim = inputStateAndRef.getState().getData().getClaims().stream().filter(correspondentClaim ->
                correspondentClaim.getClaimNumber().equals(claimNumber) && correspondentClaim.getClaimStatus().equals(this.getPreviousState())
            ).findAny().orElseThrow(() -> new IllegalArgumentException("Proposed Claim Not In Proposal State"));

            InsuranceDetail insuranceDetail = new InsuranceDetail(
                    insuranceDetailInfo.getInsuranceCompanyNumber(),
                    insuranceDetailInfo.getInsuranceCompanyPolicyNumber(),
                    insuranceDetailInfo.getField()
            );

            Party proposer = getOurIdentity();

            Claim claim = new Claim(claimNumber, inputClaim.getClaimDescription(),
                    inputClaim.getClaimAmount(), this.getNextState(), inputClaim.getInternalPolicyNo(),
                    inputClaim.getAccidentDate(), inputClaim.getEpisodeDate(), inputClaim.getAccidentType(),
                    inputClaim.getModule(), insuranceDetail, proposer, insuree);

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
                    .addCommand(new InsuranceContract.Commands.AcceptClaim(), ImmutableList.of(getOurIdentity().getOwningKey(), input.getInsuree().getOwningKey()));

            // Verify the transaction
            transactionBuilder.verify(getServiceHub());

            FlowSession session = initiateFlow(insuree);

            // We sign the transaction with our private key, making it immutable.
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // The counter party signs the transaction
            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));

            // We get the transaction notarised and recorded automatically by the platform.
            return subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
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
    public static class InsuranceAcceptanceClaimResponder extends FlowLogic<Void> {

        private FlowSession counterpartySession;

        public InsuranceAcceptanceClaimResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an InsuranceState transaction.", output instanceof InsuranceState);
                        InsuranceState insuranceState = (InsuranceState) output;
                        require.using("Transaction must have valid claim", insuranceState.getClaims().get(insuranceState.getClaims().size()-1).getClaimStatus().equals(ClaimStatus.Accepted));
                        return null;
                    });
                }
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
