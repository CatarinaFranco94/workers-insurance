package net.corda.examples.workinsurance.flows.models;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class WorkerInfo {

    private final String policyNumber;
    private final String workerName;
    private final String healthNumber;

    public WorkerInfo(String policyNumber, String workerName, String healthNumber) {
        this.policyNumber = policyNumber;
        this.workerName = workerName;
        this.healthNumber = healthNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getName() {
        return workerName;
    }

    public String getHealthNumber() {
        return healthNumber;
    }

}
