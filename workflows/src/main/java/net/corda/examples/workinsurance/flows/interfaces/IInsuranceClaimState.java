package net.corda.examples.workinsurance.flows.interfaces;

import net.corda.examples.workinsurance.states.ClaimStatus;

public interface IInsuranceClaimState {
    ClaimStatus getNextState();
    ClaimStatus getPreviousState();
}
