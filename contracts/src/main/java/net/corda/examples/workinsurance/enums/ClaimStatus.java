package net.corda.examples.workinsurance.enums;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum ClaimStatus {
    None,
    Proposal,
    Accepted,
    Rejected
}
