package net.corda.examples.workinsurance.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum ClaimStatus {
    None,
    Proposal,
    Acceptance,
    Reject
}
