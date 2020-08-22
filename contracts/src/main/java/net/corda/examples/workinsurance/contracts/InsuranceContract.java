package net.corda.examples.workinsurance.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.states.InsuranceState;

import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class InsuranceContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.examples.workinsurance.contracts.InsuranceContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), InsuranceContract.Commands.class);

        if (command.getValue() instanceof InsuranceContract.Commands.IssueInsurance) {
            verifyInsuranceIssue(tx);

        } else if(command.getValue() instanceof InsuranceContract.Commands.AddClaim) {
            verifyClaimCreation(tx);

        }else if(command.getValue() instanceof InsuranceContract.Commands.AcceptClaim) {
            verifyClaimAcceptance(tx);

        }else if(command.getValue() instanceof InsuranceContract.Commands.RejectClaim){
            verifyClaimReject(tx);

        } else {
            throw new IllegalArgumentException("Unrecognized command");
        }
    }

    private void verifyClaimReject(LedgerTransaction tx) {
        InsuranceState inputState = (InsuranceState)tx.getInput(0);
        InsuranceState outputState = (InsuranceState) tx.getOutput(0);

        Claim outputClaim = outputState.getClaims().get(outputState.getClaims().size() -1 );

        List<Claim> inputClaimListWithOutputClaimNumber = inputState.getClaims().stream()
                .filter(claimList -> claimList.getClaimNumber().equals(outputClaim.getClaimNumber()))
                .collect(Collectors.toList());

        Claim inputClaim = inputClaimListWithOutputClaimNumber.get(inputClaimListWithOutputClaimNumber.size() - 1);

        requireThat(req -> {
            req.using("Insurance transaction must have input states", (!tx.getInputStates().isEmpty()));
            req.using("Input State must have at least one claim with the same number as the output claim", (!inputClaimListWithOutputClaimNumber.isEmpty()));
            req.using("Input state claim must be in proposal status", (inputClaim.getClaimStatus().equals(ClaimStatus.Proposal)));
            req.using("Output Claim must be in reject status", outputClaim.getClaimStatus().equals(ClaimStatus.Rejected));
            return null;
        });
    }

    private void verifyInsuranceIssue(LedgerTransaction tx) {
        requireThat(req -> {
            req.using("Transaction must have no input states.", tx.getInputStates().isEmpty());
            req.using("Transaction must have output states.", (!tx.getOutputStates().isEmpty()));
            req.using("Transaction must have one output state.", tx.getOutputStates().size() == 1);
            req.using("Transaction output must be an InsuranceState", tx.getOutputStates().get(0) instanceof InsuranceState);
            req.using("Transaction must have a command", tx.getCommands().size() == 1);
            req.using("Transaction command must be Issue Command", tx.getCommands().get(0).getValue() instanceof InsuranceContract.Commands.IssueInsurance);

            InsuranceState insuranceState = (InsuranceState) tx.getOutputStates().get(0);

            req.using("Issuer must be a required signer", tx.getCommands().get(0).getSigners().contains(insuranceState.getInsurer().getOwningKey()));
            return null;
        });
    }

    private void verifyClaimCreation(LedgerTransaction tx) {
        requireThat(req -> {
            req.using("Insurance transaction must have input states, the insurance police", (!tx.getInputStates().isEmpty()));
            req.using("Insurance transaction must have one input state", tx.getInputStates().size() == 1);
            req.using("Insurance input transaction must be InsuranceState", tx.getInputStates().get(0) instanceof InsuranceState);
            req.using("Insurance transaction must have output state", (!tx.getOutputStates().isEmpty()));
            req.using("Insurance transaction must have one output state", tx.getOutputStates().size() == 1);
            req.using("Insurance output transaction must be an InsuranceState", tx.getOutputStates().get(0) instanceof InsuranceState);
            req.using("Insurance transaction must have a command", tx.getCommands().size() == 1);
            req.using("Insurance transaction command must be a AddClaim command", tx.getCommands().get(0).getValue() instanceof InsuranceContract.Commands.AddClaim);

            InsuranceState insuranceState = (InsuranceState) tx.getOutputStates().get(0);

            req.using("Issuer must be a required signer", tx.getCommands().get(0).getSigners().contains(insuranceState.getInsuree().getOwningKey()));
            return null;
        });
    }

    private void verifyClaimAcceptance(LedgerTransaction tx){
        InsuranceState inputState = (InsuranceState)tx.getInput(0);
        InsuranceState outputState = (InsuranceState) tx.getOutput(0);

        Claim outputClaim = outputState.getClaims().get(outputState.getClaims().size() - 1 );

        List<Claim> inputClaimListWithOutputClaimNumber = inputState.getClaims().stream()
                .filter(claimList -> claimList.getClaimNumber().equals(outputClaim.getClaimNumber()))
                .collect(Collectors.toList());

        Claim inputClaim = inputClaimListWithOutputClaimNumber.get(inputClaimListWithOutputClaimNumber.size() - 1);


        requireThat(req -> {
            req.using("Insurance transaction must have input states", (!tx.getInputStates().isEmpty()));
            req.using("Input State must have at least one claim with the same number as the output claim", (!inputClaimListWithOutputClaimNumber.isEmpty()));
            req.using("Input state claim must be in proposal status", (inputClaim.getClaimStatus().equals(ClaimStatus.Proposal)));
            req.using("Output Claim must be in acceptance status", outputClaim.getClaimStatus().equals(ClaimStatus.Accepted));
            return null;
        });
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class IssueInsurance implements Commands {}
        class AddClaim implements Commands {}
        class AcceptClaim implements Commands {}
        class RejectClaim implements Commands {}
    }
}