package net.corda.examples.workinsurance;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.flows.models.InsuranceInfo;
import net.corda.examples.workinsurance.flows.implementations.IssueInsuranceFlow;
import net.corda.examples.workinsurance.flows.models.WorkerInfo;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.examples.workinsurance.states.WorkerDetail;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IssueInsuranceFlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
        TestCordapp.findCordapp("net.corda.examples.workinsurance.contracts"),
        TestCordapp.findCordapp("net.corda.examples.workinsurance.flows")
    )));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();


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

        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), output.getNotary());
    }

    @Test
    public void transactionConstructedByFlowHasOneInsuranceStateOutputWithTheCorrectInsureeAndInsuredValue() throws Exception {

        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        InsuranceState output = signedTransaction.getTx().outputsOfType(InsuranceState.class).get(0);

        assertEquals(b.getInfo().getLegalIdentities().get(0), output.getInsuree());
        assertEquals(20000, output.getInsuredValue());
    }

    @Test
    public void transactionConstructedByFlowHasOneOutputUsingTheCorrectContract() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals("net.corda.examples.workinsurance.contracts.InsuranceContract", output.getContract());
    }

    @Test
    public void transactionConstructedByFlowHasOneIssueCommand() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert(command.getValue() instanceof InsuranceContract.Commands.IssueInsurance);
    }

    @Test
    public void transactionConstructedByFlowHasOneCommandWithTheIssuerAndTheOwnerAsASigners() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assertEquals(2, command.getSigners().size());
        assertTrue(command.getSigners().contains(a.getInfo().getLegalIdentities().get(0).getOwningKey()));
        assertTrue(command.getSigners().contains(b.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void transactionConstructedByFlowHasNoInputsAttachmentsOrTimeWindows() throws Exception {
        WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
        InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);
        IssueInsuranceFlow.IssueInsuranceInitiator flow = new IssueInsuranceFlow.IssueInsuranceInitiator(insuranceInfo, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(0, signedTransaction.getTx().getInputs().size());
        // The single attachment is the contract attachment.
        assertEquals(1, signedTransaction.getTx().getAttachments().size());
        assertNull(signedTransaction.getTx().getTimeWindow());
    }
}
