package net.corda.examples.workinsurance;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.enums.Module;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.states.InsuranceDetail;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.examples.workinsurance.states.WorkerDetail;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity hsj = new TestIdentity(new CordaX500Name("HSJ", "", "GB"));
    private final TestIdentity ags = new TestIdentity(new CordaX500Name("AGS", "", "GB"));

    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));

    private final WorkerDetail workerDetail = new WorkerDetail("policyNr", "Alfredo", "123456", "CSW" );
    private final InsuranceState insuranceState = new InsuranceState(1000, 24, ags.getParty(), hsj.getParty(), workerDetail, null);

    private final Date accidentDate = new Date(2019, 10, 12);

    private final InsuranceDetail insuranceDetail = new InsuranceDetail("InsuranceCompanyNr", "InsuranceCompanyPolicyNr",
            "Field");

    private final Claim claimProposal = new Claim("N1", "Minor accident", 200, ClaimStatus.Proposal,
            "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
            null, hsj.getParty(), ags.getParty());

    private final Claim claimAccepted = new Claim("N1", "Minor accident", 200, ClaimStatus.Accepted,
            "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
            insuranceDetail, hsj.getParty(), ags.getParty());

    private final Claim claimRejected = new Claim("N1", "Minor accident", 200, ClaimStatus.Rejected,
            "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
            null, hsj.getParty(), ags.getParty());

    private final InsuranceState insuranceStateClaimProposal = new InsuranceState(insuranceState.getInsuredValue(), insuranceState.getDuration(), insuranceState.getInsurer(),
            insuranceState.getInsuree(), insuranceState.getWorkerDetail(), Arrays.asList(claimProposal));

    private final InsuranceState insuranceStateClaimAccepted = new InsuranceState(insuranceState.getInsuredValue(), insuranceState.getDuration(), insuranceState.getInsurer(),
            insuranceState.getInsuree(), insuranceState.getWorkerDetail(), Arrays.asList(claimAccepted));

    private final InsuranceState insuranceStateClaimRejected = new InsuranceState(insuranceState.getInsuredValue(), insuranceState.getDuration(), insuranceState.getInsurer(),
            insuranceState.getInsuree(), insuranceState.getWorkerDetail(), Arrays.asList(claimRejected));

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

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOneInputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two input, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one input, will verify
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresInputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            // Has wrong input type, will fail.
            tx.input(InsuranceContract.ID, new DummyState());
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct input type, will verify
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has two output, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has one output, will verify
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOutputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has wrong output type, will fail.
            tx.output(InsuranceContract.ID, new DummyState());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has correct output type, will verify
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOneCommandInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            // Has two commands, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            // Has one command, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresCommandToBeAcceptClaimCommand(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            // Has wrong command type, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            // Has correct command type, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresInputClaimsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input does not have claims, will fail.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input have claims, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresInputClaimInProposedStateInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input does not have claim in Proposed, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does have claim in Proposal, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresInputClaimWithoutInsuranceDetailInfoInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input has claim with insurance detail info, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does not have claim with insurance detail info, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresInputToHaveTheSameClaimNoAsOutputClaim(){
        Claim claimProposal = new Claim("N2", "Minor accident", 200, ClaimStatus.Proposal,
                "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
                null, hsj.getParty(), ags.getParty());
        InsuranceState insuranceStateClaimProposalWithoutClaimNo = new InsuranceState(insuranceState.getInsuredValue(), insuranceState.getDuration(), insuranceState.getInsurer(),
                insuranceState.getInsuree(), insuranceState.getWorkerDetail(), Arrays.asList(claimProposal));

        transaction(ledgerServices, tx -> {
            // Input has not claim No equals to output claim No, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposalWithoutClaimNo);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does not have claim with insurance detail info, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOutputClaimsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claims, will fail.
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does have claims, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOutputClaimInAcceptStateInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claim in Accepted, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does have claim in Accept, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresOutputClaimWithInsuranceDetailInfoInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claim with insurance detail info, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does have claim with insurance detail info, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractAcceptClaimCommandRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        InsuranceState insuranceStateWhereHSJIsIssuer = new InsuranceState(1000, 12, hsj.getParty(), ags.getParty(), workerDetail, Arrays.asList(claimProposal, claimAccepted));

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            // Issuer is not a required signer, will fail.
            tx.command(hsj.getPublicKey(), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(ags.getPublicKey(), new InsuranceContract.Commands.AcceptClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.AcceptClaim());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void insuranceContractRejectClaimCommandRequiresOneInputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two input, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one input, will verify
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresInputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            // Has wrong input type, will fail.
            tx.input(InsuranceContract.ID, new DummyState());
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct input type, will verify
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has two output, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has one output, will verify
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresOutputToBeAnInsuranceState(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has wrong output type, will fail.
            tx.output(InsuranceContract.ID, new DummyState());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Has correct output type, will verify
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresOneCommandInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            // Has two commands, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            // Has one command, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresCommandToBeAcceptClaimCommand(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            // Has wrong command type, will fail.
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            // Has correct command type, will verify
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresInputClaimsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input does not have claims, will fail.
            tx.input(InsuranceContract.ID, insuranceState);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input have claims, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresInputClaimInProposedStateInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input does not have claim in Proposed, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does have claim in Proposal, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresInputClaimWithoutInsuranceDetailInfoInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Input has claim with insurance detail info, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does not have claim with insurance detail info, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresInputToHaveTheSameClaimNoAsOutputClaim(){
        Claim claimProposal = new Claim("N2", "Minor accident", 200, ClaimStatus.Proposal,
                "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
                null, hsj.getParty(), ags.getParty());
        InsuranceState insuranceStateClaimProposalWithoutClaimNo = new InsuranceState(insuranceState.getInsuredValue(), insuranceState.getDuration(), insuranceState.getInsurer(),
                insuranceState.getInsuree(), insuranceState.getWorkerDetail(), Arrays.asList(claimProposal));

        transaction(ledgerServices, tx -> {
            // Input has not claim No equals to output claim No, will fail.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposalWithoutClaimNo);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Input does not have claim with insurance detail info, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void insuranceContractRejectClaimCommandRequiresOutputClaimsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claims, will fail.
            tx.output(InsuranceContract.ID, insuranceState);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does have claims, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresOutputClaimInRejectStateInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claim in Rejected, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does have claim in Rejected, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresOutputClaimWithoutInsuranceDetailInfoInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output have claim with insurance detail info, will fail.
            tx.output(InsuranceContract.ID, insuranceStateClaimAccepted);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            // Output does not have claim with insurance detail info, will verify.
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void insuranceContractRejectClaimCommandRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        InsuranceState insuranceStateWhereHSJIsIssuer = new InsuranceState(1000, 12, hsj.getParty(), ags.getParty(), workerDetail, Arrays.asList(claimProposal, claimRejected));

        transaction(ledgerServices, tx -> {
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            // Issuer is not a required signer, will fail.
            tx.command(hsj.getPublicKey(), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(ags.getPublicKey(), new InsuranceContract.Commands.RejectClaim());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateClaimRejected);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.input(InsuranceContract.ID, insuranceStateClaimProposal);
            tx.output(InsuranceContract.ID, insuranceStateWhereHSJIsIssuer);
            tx.command(Arrays.asList(ags.getPublicKey(), hsj.getPublicKey()), new InsuranceContract.Commands.RejectClaim());
            tx.verifies();
            return null;
        });
    }

}
