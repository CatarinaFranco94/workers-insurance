package net.corda.examples.workinsurance.schema;

import net.corda.core.schemas.PersistentState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * JPA Entity for saving insurance details to the database table
 */
@Entity
@Table(name = "INSURANCE_DETAIL")
public class PersistentInsurance extends PersistentState implements Serializable {

    @Column private final Long insuredValue;
    @Column private final Integer duration;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "id", referencedColumnName = "id"),
            @JoinColumn(name = "policyNumber", referencedColumnName = "policyNumber"),
    })
    private final PersistentWorker worker;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "output_index", referencedColumnName = "output_index"),
            @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    })
    private List<PersistentClaim> claims;

    /**
     * Default constructor required by Hibernate
     */
    public PersistentInsurance() {
        this.insuredValue = null;
        this.duration = null;
        this.worker = null;
        this.claims = null;
    }

    public PersistentInsurance(Long insuredValue, Integer duration, PersistentWorker worker,
                               List<PersistentClaim> claims) {
        this.insuredValue = insuredValue;
        this.duration = duration;
        this.worker = worker;
        this.claims = claims;
    }

    public Long getInsuredValue() {
        return insuredValue;
    }

    public Integer getDuration() {
        return duration;
    }

    public PersistentWorker getWorker() {
        return worker;
    }

    public List<PersistentClaim> getClaims() {
        return claims;
    }
}
