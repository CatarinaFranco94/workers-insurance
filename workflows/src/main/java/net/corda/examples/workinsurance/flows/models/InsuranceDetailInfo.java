package net.corda.examples.workinsurance.flows.models;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class InsuranceDetailInfo {

    private final String insuranceCompanyNumber;
    private final String insuranceCompanyPolicyNumber;
    private final String field;

    public InsuranceDetailInfo(String insuranceCompanyNumber, String insuranceCompanyPolicyNumber, String field) {
        this.insuranceCompanyNumber = insuranceCompanyNumber;
        this.insuranceCompanyPolicyNumber = insuranceCompanyPolicyNumber;
        this.field = field;
    }

    public String getInsuranceCompanyNumber() {
        return insuranceCompanyNumber;
    }

    public String getInsuranceCompanyPolicyNumber() {
        return insuranceCompanyPolicyNumber;
    }

    public String getField() {
        return field;
    }
}
