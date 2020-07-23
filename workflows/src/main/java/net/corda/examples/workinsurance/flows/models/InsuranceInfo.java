package net.corda.examples.workinsurance.flows.models;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class InsuranceInfo {

    private final WorkerInfo workerInfo;

    private final long insuredValue;
    private final int duration;

    public InsuranceInfo(long insuredValue, int duration, WorkerInfo workerInfo) {
        this.insuredValue = insuredValue;
        this.duration = duration;
        this.workerInfo = workerInfo;
    }

    public long getInsuredValue() {
        return insuredValue;
    }

    public int getDuration() {
        return duration;
    }

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }
}
