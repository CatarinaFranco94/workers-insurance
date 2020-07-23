package net.corda.examples.workinsurance.flows.interfaces;

import net.corda.examples.workinsurance.enums.ClaimStatus;

public interface IInsuranceClaimState {
    ClaimStatus getNextState();
    ClaimStatus getPreviousState();
}
