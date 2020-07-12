package net.corda.examples.workinsurance.flows;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class InsuranceInfo {

    private final WorkerInfo workerInfo;

    private final String policyNumber;
    private final long insuredValue;
    private final int duration;
    private final int premium;

    public InsuranceInfo(String policyNumber, long insuredValue, int duration, int premium, WorkerInfo workerInfo) {
        this.policyNumber = policyNumber;
        this.insuredValue = insuredValue;
        this.duration = duration;
        this.premium = premium;
        this.workerInfo = workerInfo;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public long getInsuredValue() {
        return insuredValue;
    }

    public int getDuration() {
        return duration;
    }

    public int getPremium() {
        return premium;
    }

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }
}
