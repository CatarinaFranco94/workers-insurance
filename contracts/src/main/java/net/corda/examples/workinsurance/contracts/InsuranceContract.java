package net.corda.examples.workinsurance.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.states.ClaimStatus;
import net.corda.examples.workinsurance.states.InsuranceState;

import java.util.ArrayList;
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

        } else {
            throw new IllegalArgumentException("Unrecognized command");
        }
    }

    private void verifyInsuranceIssue(LedgerTransaction tx) {
        requireThat(req -> {
            req.using("Transaction must have no input states.", tx.getInputStates().isEmpty());
            return null;
        });
    }

    private void verifyClaimCreation(LedgerTransaction tx) {
        requireThat(req -> {
            req.using("Insurance transaction must have input states, the insurance police", (!tx.getInputStates().isEmpty()));
            return null;
        });
    }

    private void verifyClaimAcceptance(LedgerTransaction tx){
        InsuranceState inputState = (InsuranceState)tx.getInput(0);
        InsuranceState outputState = (InsuranceState) tx.getOutput(0);

        List<Claim> inputProposalClaims = inputState.getClaims().stream()
                .filter(correspondentClaim -> correspondentClaim.getClaimStatus().equals(ClaimStatus.Proposal))
                .collect(Collectors.toList());

        Claim ouputClaim = outputState.getClaims().get(outputState.getClaims().size() -1);

        List<Claim> inputClaimsWithOutputClaimNo = inputProposalClaims.stream()
                .filter(correspondentClaim -> correspondentClaim.getClaimNumber().equals(ouputClaim.getClaimNumber()))
                .collect(Collectors.toList());

        requireThat(req -> {
            req.using("Insurance transaction must have input states", (!tx.getInputStates().isEmpty()));
            req.using("Input State must have at least one claim in proposal state", (!inputProposalClaims.isEmpty()));
            req.using("Input state must have ONE claim with the same number as the output claim and in proposal status", (inputClaimsWithOutputClaimNo.size() == 1));
            req.using("Output State must be in acceptance status", ouputClaim.getClaimStatus().equals(ClaimStatus.Acceptance));
            return null;
        });
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class IssueInsurance implements Commands {}
        class AddClaim implements Commands {}
        class AcceptClaim implements Commands {}
    }
}