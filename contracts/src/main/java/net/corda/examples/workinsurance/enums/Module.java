package net.corda.examples.workinsurance.enums;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum Module {
    None,
    Urgency,
    DayHospital,
    ExternConsult,
    Internment,
    OperatingRoom
}
