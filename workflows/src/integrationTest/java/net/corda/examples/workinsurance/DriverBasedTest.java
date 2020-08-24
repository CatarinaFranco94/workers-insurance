package net.corda.examples.workinsurance;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.examples.workinsurance.flows.implementations.InsuranceClaimFlow;
import net.corda.examples.workinsurance.flows.implementations.IssueInsuranceFlow;
import net.corda.examples.workinsurance.flows.models.InsuranceInfo;
import net.corda.examples.workinsurance.flows.models.WorkerInfo;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import org.junit.Test;

import java.util.List;

import static net.corda.testing.driver.Driver.driver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DriverBasedTest {
    private final TestIdentity insuranceAGS = new TestIdentity(new CordaX500Name("InsuranceAGS", "", "GB"));
    private final TestIdentity hospitalA = new TestIdentity(new CordaX500Name("HospitalA", "", "US"));
    private final TestIdentity hospitalB = new TestIdentity(new CordaX500Name("HospitalB", "", "PT"));

    @Test
    public void nodeTest() {
        driver(new DriverParameters().withIsDebug(true).withStartNodesInProcess(true), dsl -> {
            // Start a pair of nodes and wait for them both to be ready.
            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().withProvidedName(insuranceAGS.getName())),
                    dsl.startNode(new NodeParameters().withProvidedName(hospitalA.getName()))
            );

            try {
                NodeHandle partyAHandle = handleFutures.get(0).get();
                NodeHandle partyBHandle = handleFutures.get(1).get();

                // From each node, make an RPC call to retrieve another node's name from the network map, to verify that the
                // nodes have started and can communicate.

                // This is a very basic test: in practice tests would be starting flows, and verifying the states in the vault
                // and other important metrics to ensure that your CorDapp is working as intended.
                assertEquals(partyAHandle.getRpc().wellKnownPartyFromX500Name(hospitalA.getName()).getName(), hospitalA.getName());
                assertEquals(partyBHandle.getRpc().wellKnownPartyFromX500Name(insuranceAGS.getName()).getName(), insuranceAGS.getName());
            } catch (Exception e) {
                throw new RuntimeException("Caught exception during test: ", e);
            }

            return null;
        });
    }

    @Test
    public void issueInsuranceFlowTest() {
        driver(new DriverParameters().withIsDebug(true).withStartNodesInProcess(true), dsl -> {
            // Start a pair of nodes and wait for them both to be ready.
            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().withProvidedName(insuranceAGS.getName())),
                    dsl.startNode(new NodeParameters().withProvidedName(hospitalA.getName())),
                    dsl.startNode(new NodeParameters().withProvidedName(hospitalB.getName()))
            );

            try {
                NodeHandle partyAHandle = handleFutures.get(0).get();
                NodeHandle partyBHandle = handleFutures.get(1).get();
                NodeHandle partyCHandle = handleFutures.get(2).get();

                Party partyA = partyAHandle.getNodeInfo().getLegalIdentities().get(0);
                Party partyB = partyBHandle.getNodeInfo().getLegalIdentities().get(0);


                WorkerInfo workerInfo = new WorkerInfo("policyNr", "Alfredo", "123456", "CSW" );
                InsuranceInfo insuranceInfo = new InsuranceInfo(20000, 20, workerInfo);

                // Run issue transaction using rpc
                partyAHandle.getRpc().startTrackedFlowDynamic(IssueInsuranceFlow.IssueInsuranceInitiator.class, insuranceInfo, partyB)
                        .getReturnValue().get();

                // Query Node A
                Vault.Page<InsuranceState> insuranceStateStates_A = partyAHandle.getRpc().vaultQuery(InsuranceState.class);
                assertEquals(1, insuranceStateStates_A.getStates().size());

                InsuranceState insururanceState_A = insuranceStateStates_A.getStates().get(0).getState().getData();
                assertEquals(partyA, insururanceState_A.getInsurer());
                assertEquals(partyB, insururanceState_A.getInsuree());
                assertNotNull(insururanceState_A.getWorkerDetail());


                // Query Node B
                Vault.Page<InsuranceState> tokenStates_B = partyBHandle.getRpc().vaultQuery(InsuranceState.class);
                assertEquals(1, tokenStates_B.getStates().size());

                InsuranceState insuranceState_B = tokenStates_B.getStates().get(0).getState().getData();
                assertEquals(partyA, insuranceState_B.getInsurer());
                assertEquals(partyB, insuranceState_B.getInsuree());
                assertNotNull(insuranceState_B.getWorkerDetail());


                //Query Node C
                Vault.Page<InsuranceState> insuranceStates_C = partyCHandle.getRpc().vaultQuery(InsuranceState.class);
                assertEquals(0, insuranceStates_C.getStates().size());

            } catch (Exception e) {
                throw new RuntimeException("Caught exception during test: ", e);
            }

            return null;
        });
    }
}