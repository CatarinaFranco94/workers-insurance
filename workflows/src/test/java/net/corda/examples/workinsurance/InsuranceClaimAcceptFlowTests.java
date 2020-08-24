package net.corda.examples.workinsurance;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.flows.implementations.InsuranceAcceptanceClaimFlow;
import net.corda.examples.workinsurance.flows.implementations.InsuranceClaimFlow;
import net.corda.examples.workinsurance.flows.implementations.IssueInsuranceFlow;
import net.corda.examples.workinsurance.flows.models.ClaimInfo;
import net.corda.examples.workinsurance.flows.models.InsuranceDetailInfo;
import net.corda.examples.workinsurance.flows.models.InsuranceInfo;
import net.corda.examples.workinsurance.flows.models.WorkerInfo;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class InsuranceClaimAcceptFlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("net.corda.examples.workinsurance.contracts"),
            TestCordapp.findCordapp("net.corda.examples.workinsurance.flows")
    )));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    private final Date accidentDate = new Date(2019,10,12);
    private final ClaimInfo claimInfo = new ClaimInfo("N1", "Minor accident", 200,
            "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, "DayHospital");

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void transactionConstructedByFlowUsesTheCorrectNotary() throws Exception {

        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator issueClaimflow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(issueClaimflow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction transaction = future.get();

        assertEquals(1, transaction.getTx().getOutputStates().size());
        TransactionState output = transaction.getTx().getOutputs().get(0);

        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), output.getNotary());
    }

    @Test
    public void transactionConstructedByFlowHasOneInsuranceStateOutputWithTheCorrectClaimInfo() throws Exception {

        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator issueClaimflow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(issueClaimflow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        InsuranceState output = signedTransaction.getTx().outputsOfType(InsuranceState.class).get(0);

        Claim outputClaim = output.getClaims().get(output.getClaims().size()-1);

        assertEquals(claimInfo.getClaimNumber(), outputClaim.getClaimNumber());
        assertEquals(ClaimStatus.Accepted, outputClaim.getClaimStatus());
        assertNotNull(outputClaim.getInsuranceDetail());
    }

    @Test
    public void transactionConstructedByFlowHasOneOutputUsingTheCorrectContract() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator issueClaimflow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(issueClaimflow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();


        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals("net.corda.examples.workinsurance.contracts.InsuranceContract", output.getContract());
    }

    @Test
    public void transactionConstructedByFlowHasOneAcceptClaimCommand() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator issueClaimflow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(issueClaimflow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert(command.getValue() instanceof InsuranceContract.Commands.AcceptClaim);
    }

    @Test
    public void transactionConstructedByFlowHasOneCommandWithTheIssuerAndTheOwnerAsASigners() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator claimIssueFlow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(claimIssueFlow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assertEquals(2, command.getSigners().size());
        assertTrue(command.getSigners().contains(a.getInfo().getLegalIdentities().get(0).getOwningKey()));
        assertTrue(command.getSigners().contains(b.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void transactionConstructedByFlowHasNoInputsAttachmentsOrTimeWindows() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator insuranceIssueFlow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        a.startFlow(insuranceIssueFlow);
        network.runNetwork();

        InsuranceClaimFlow.InsuranceClaimInitiator issueClaimflow = new InsuranceClaimFlow.InsuranceClaimInitiator(claimInfo, workerInfo.getPolicyNumber());
        b.startFlow(issueClaimflow);
        network.runNetwork();

        InsuranceDetailInfo insuranceDetailInfo = new InsuranceDetailInfo("CompNr", "PolNr", "field");
        InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator flow = new InsuranceAcceptanceClaimFlow.InsuranceAcceptanceClaimInitiator(insuranceDetailInfo, workerInfo.getPolicyNumber(),claimInfo.getClaimNumber(),b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        // There is one input that is the insurance issue
        assertEquals(1, signedTransaction.getTx().getInputs().size());
        // The single attachment is the contract attachment.
        assertEquals(1, signedTransaction.getTx().getAttachments().size());
        assertNull(signedTransaction.getTx().getTimeWindow());
    }
}
