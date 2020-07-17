package net.corda.examples.workinsurance.states;


import net.corda.core.serialization.CordaSerializable;

/**
 * Simple POJO class for the worker details.
 * Corda uses its own serialization framework hence the class needs to be annotated with @CordaSerializable, so that
 * the objects of the class can be serialized to be passed across different nodes.
 */
@CordaSerializable
public class WorkerDetail {

    private final String policyNumber;
    private final String name;
    private final String healthNumber;

    public WorkerDetail(String policyNumber, String name, String healthNumber) {
        this.policyNumber = policyNumber;
        this.name = name;
        this.healthNumber = healthNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getName() {
        return name;
    }

    public String getHealthNumber() {
        return healthNumber;
    }
}