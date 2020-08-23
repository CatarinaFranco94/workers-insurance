package net.corda.examples.workinsurance.flows.implementations;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.Module;
import net.corda.examples.workinsurance.flows.models.ClaimInfo;
import net.corda.examples.workinsurance.flows.interfaces.IInsuranceClaimState;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.states.InsuranceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class InsuranceClaimFlow {

    private InsuranceClaimFlow(){}

    @InitiatingFlow
    @StartableByRPC
    public static class InsuranceClaimInitiator extends FlowLogic<SignedTransaction> implements IInsuranceClaimState {

        private final ProgressTracker progressTracker = new ProgressTracker();

        private final ClaimInfo claimInfo;
        private final String policyNumber;

        private final static Logger logger = LoggerFactory.getLogger(InsuranceClaimFlow.InsuranceClaimInitiator.class);

        public InsuranceClaimInitiator(ClaimInfo claimInfo, String policyNumber) {
            this.claimInfo = claimInfo;
            this.policyNumber = policyNumber;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            Party insureeOurIdentity = getOurIdentity();

            // Query the vault to fetch a list of all Insurance state, and filter the results based on the policyNumber
            // to fetch the desired Insurance state from the vault. This filtered state would be used as input to the
            // transaction.
            List<StateAndRef<InsuranceState>> insuranceStateAndRefs = getServiceHub().getVaultService()
                    .queryBy(InsuranceState.class).getStates();

            StateAndRef<InsuranceState> inputStateAndRef = insuranceStateAndRefs.stream().filter(insuranceStateAndRef -> {
                InsuranceState insuranceState = insuranceStateAndRef.getState().getData();
                return insuranceState.getWorkerDetail().getPolicyNumber().equals(policyNumber) && insuranceState.getInsuree().equals(insureeOurIdentity);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Insuree Policy Not Found"));

            Claim claim = new Claim(claimInfo.getClaimNumber(), claimInfo.getClaimDescription(),
                    claimInfo.getClaimAmount(), this.getNextState(), claimInfo.getInternalPolicyNo(), claimInfo.getAccidentDate(),
                    claimInfo.getEpisodeDate(), AccidentType.WorkAccident, Module.valueOf(claimInfo.getModule()), null,
                    insureeOurIdentity, inputStateAndRef.getState().getData().getInsurer());
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
                    .addCommand(new InsuranceContract.Commands.AddClaim(), ImmutableList.of(getOurIdentity().getOwningKey(), input.getInsurer().getOwningKey()));

            // Verify the transaction
            transactionBuilder.verify(getServiceHub());

            FlowSession session = initiateFlow(input.getInsurer());

            // Sign the transaction
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // The counter party signs the transaction
            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));

            // We get the transaction notarised and recorded automatically by the platform.
            return subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
        }

        @Override
        public ClaimStatus getNextState() {
            return ClaimStatus.Proposal;
        }

        @Override
        public ClaimStatus getPreviousState() {
            return ClaimStatus.None;
        }
    }

    @InitiatedBy(InsuranceClaimInitiator.class)
    public static class InsuranceClaimResponder extends FlowLogic<Void> {

        private FlowSession counterpartySession;

        public InsuranceClaimResponder(FlowSession counterpartySession) {
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
                        require.using("Transaction must have valid claim", insuranceState.getClaims().get(0).getClaimStatus().equals(ClaimStatus.Proposal));
                        return null;
                    });
                }
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
