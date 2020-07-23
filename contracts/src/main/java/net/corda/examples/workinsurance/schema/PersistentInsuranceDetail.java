package net.corda.examples.workinsurance.schema;

import javax.persistence.*;
import java.util.UUID;


/**
 * JPA Entity for saving claim details to the database table
 */
@Embeddable
public class PersistentInsuranceDetail {

    private final String insuranceCompanyNumber;

    private final String insuranceCompanyPolicyNumber;

    private final String field;

    public PersistentInsuranceDetail(){
        this.insuranceCompanyNumber = null;
        this.insuranceCompanyPolicyNumber = null;
        this.field = null;
    }

    public PersistentInsuranceDetail(String insuranceCompanyNumber, String insuranceCompanyPolicyNumber, String field) {
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
