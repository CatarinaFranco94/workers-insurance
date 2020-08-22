package net.corda.examples.workinsurance;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.examples.workinsurance.states.WorkerDetail;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity hsj = new TestIdentity(new CordaX500Name("HSJ", "", "GB"));
    private final TestIdentity ags = new TestIdentity(new CordaX500Name("AGS", "", "GB"));

    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));

    private WorkerDetail workerDetail = new WorkerDetail("policyNr", "Alfredo", "123456", "CSW" );
    private InsuranceState insuranceState = new InsuranceState(1000, 24, ags.getParty(), hsj.getParty(), workerDetail, null);

    @Test
    public void insuranceContractImplementsContract(){
        assert(new InsuranceContract() instanceof Contract);
    }

    @Test
    public void insuranceContractIssueCommandRequiresZeroInputsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractIssueCommandRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRequiresTheTransactionOutputToBeAInsuranceState(){
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(InsuranceContract.ID, new DummyState());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractIssueCommandRequiresOneCommandInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(InsuranceContract.ID, insuranceState);
            // Has two commands, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(InsuranceContract.ID, insuranceState);
            // Has one command, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRequiresTheTransactionCommandToBeAnIssueInsuranceCommand(){
        transaction(ledgerServices, tx -> {
            tx.output(InsuranceContract.ID, insuranceState);
            // Has wrong command type, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(InsuranceContract.ID, insuranceState);
            // Has correct command type, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        InsuranceState insuranceStateWhereHSJIsIssuer = new InsuranceState(1000, 12, hsj.getParty(), ags.getParty(), workerDetail, null);

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail.
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(hsj.getPublicKey(), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(ags.getPublicKey(), new InsuranceContract.Commands.IssueInsurance());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
           // Issuer is a required signer, will verify.
           tx.output(InsuranceContract.ID, insuranceState);
           tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
           tx.verifies();
           return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.IssueInsurance());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresOneInputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two input, will fail.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one input, will verify
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresInputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            // Has wrong input type, will fail.
            tx.input(InsuranceContract.ID, new DummyState());
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct input type, will verify
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            // Has two output, will fail.
            tx.output(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresOutputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            // Has wrong output type, will fail.
            tx.output(InsuranceContract.ID, new DummyState());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            // Has correct output type, will verify
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresOneCommandInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            // Has two commands, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            // Has one command, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresCommandToBeAddClaimCommand(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            // Has wrong command type, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            // Has correct command type, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAddClaimCommandRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        InsuranceState insuranceStateWhereHSJIsIssuer = new InsuranceState(1000, 12, hsj.getParty(), ags.getParty(), workerDetail, null);

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(ags.getPublicKey(), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(hsj.getPublicKey(), new InsuranceContract.Commands.AddClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(hsj.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AddClaim());
            tx.verifies();
            return null;
        });
    }


}
