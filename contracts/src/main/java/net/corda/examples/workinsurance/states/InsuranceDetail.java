package net.corda.examples.workinsurance.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class InsuranceDetail {
    private final String insuranceCompanyNumber;
    private final String insuranceCompanyPolicyNumber;
    private final String field;

    public InsuranceDetail(String insuranceCompanyNumber, String insuranceCompanyPolicyNumber, String field) {
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
