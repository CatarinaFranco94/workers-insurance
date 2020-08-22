package net.corda.examples.workinsurance.flows.implementations;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.flows.models.InsuranceInfo;
import net.corda.examples.workinsurance.flows.models.WorkerInfo;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.examples.workinsurance.states.WorkerDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueInsuranceFlow {

    private IssueInsuranceFlow(){}

    @InitiatingFlow
    @StartableByRPC
    public static class IssueInsuranceInitiator extends FlowLogic<SignedTransaction> {

        private final ProgressTracker progressTracker = new ProgressTracker();

        private final InsuranceInfo insuranceInfo;
        private final Party insuree;


        private final static Logger logger = LoggerFactory.getLogger(IssueInsuranceFlow.IssueInsuranceInitiator.class);

        public IssueInsuranceInitiator(InsuranceInfo insuranceInfo, Party insuree) {
            this.insuranceInfo = insuranceInfo;
            this.insuree = insuree;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // Obtain a reference to a notary we wish to use.
            /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
            // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

            Party insurer = getOurIdentity();

            WorkerInfo workerInfo = insuranceInfo.getWorkerInfo();
            WorkerDetail workerDetail = new WorkerDetail(workerInfo.getPolicyNumber(), workerInfo.getName(), workerInfo.getHealthNumber(), workerInfo.getPolicyHolder());

            // Build the insurance output state.
            InsuranceState insurance = new InsuranceState(insuranceInfo.getInsuredValue(),
                    insuranceInfo.getDuration(), insurer, insuree, workerDetail,null);

            // Build the transaction
            TransactionBuilder builder = new TransactionBuilder(notary)
                .addOutputState(insurance, InsuranceContract.ID)
                .addCommand(new InsuranceContract.Commands.IssueInsurance(), ImmutableList.of(insurer.getOwningKey()));

            // Verify the transaction
            builder.verify(getServiceHub());

            // Sign the transaction
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

            // Call finality Flow
            FlowSession ownerSession = initiateFlow(insuree);
            return subFlow(new FinalityFlow(selfSignedTransaction, ImmutableList.of(ownerSession)));
        }
    }

    @InitiatedBy(IssueInsuranceInitiator.class)
    public static class IssueInsuranceResponder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

        public IssueInsuranceResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }
    }
}
