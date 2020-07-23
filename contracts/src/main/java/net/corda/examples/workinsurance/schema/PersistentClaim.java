package net.corda.examples.workinsurance.schema;

import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.enums.Module;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;


/**
 * JPA Entity for saving claim details to the database table
 */
@Entity
@Table(name = "CLAIM_DETAIL")
public class PersistentClaim {

    @Id private final UUID id;
    @Column private final String claimNumber;

    @Column private final String claimDescription;

    @Column private final Integer claimAmount;

    @Column private final String internalPolicyNo;

    @Column private final Date accidentDate;

    @Column private final Date episodeDate;

    @Enumerated(EnumType.STRING)
    @Column private final AccidentType accidentType;

    @Enumerated(EnumType.STRING)
    @Column private final Module module;

    @Enumerated(EnumType.STRING)
    @Column private final ClaimStatus claimStatus;

    /**
     * Default constructor required by Hibernate
     */
    public PersistentClaim() {
        this.id = null;
        this.claimNumber = null;
        this.claimDescription = null;
        this.claimAmount = null;
        this.claimStatus = ClaimStatus.None;
        this.internalPolicyNo = null;
        this.accidentDate = null;
        this.episodeDate = null;
        this.accidentType = AccidentType.None;
        this.module = Module.None;
    }

    public PersistentClaim(String claimNumber,
                           String claimDescription,
                           int claimAmount,
                           ClaimStatus claimStatus,
                           String internalPolicyNo,
                           Date accidentDate,
                           Date episodeDate,
                           AccidentType accidentType,
                           Module module) {
        this.id = UUID.randomUUID();
        this.claimNumber = claimNumber;
        this.claimDescription = claimDescription;
        this.claimAmount = claimAmount;
        this.claimStatus = claimStatus;
        this.internalPolicyNo = internalPolicyNo;
        this.accidentDate = accidentDate;
        this.episodeDate = episodeDate;
        this.accidentType = accidentType;
        this.module = module;
    }

    public UUID getId() {
        return id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public String getClaimDescription() {
        return claimDescription;
    }

    public Integer getClaimAmount() {
        return claimAmount;
    }

    public ClaimStatus getClaimStatus() { return claimStatus; }

    public String getInternalPolicyNo() { return internalPolicyNo; }

    public Date getAccidentDate() { return accidentDate; }

    public Date getEpisodeDate() { return episodeDate; }

    public AccidentType getAccidentType() { return accidentType; }

    public Module getModule() { return module; }
}
