package net.corda.examples.workinsurance.flows.models;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class WorkerInfo {

    private final String policyNumber;
    private final String workerName;
    private final String healthNumber;
    private final String policyHolder;

    public WorkerInfo(String policyNumber, String workerName, String healthNumber, String policyHolder) {
        this.policyNumber = policyNumber;
        this.workerName = workerName;
        this.healthNumber = healthNumber;
        this.policyHolder = policyHolder;
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

    public String getPolicyHolder() { return policyHolder; }

}
