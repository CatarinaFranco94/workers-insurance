package net.corda.examples.workinsurance.states;

import net.corda.core.serialization.CordaSerializable;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.enums.Module;

import java.util.Date;

/**
 * Simple POJO class for the claim details.
 * Corda uses its own serialization framework hence the class needs to be annotated with @CordaSerializable, so that
 * the objects of the class can be serialized to be passed across different nodes.
 */
@CordaSerializable
public class Claim {

    private final String claimNumber;
    private final String claimDescription;
    private final int claimAmount;
    private final String internalPolicyNo;
    private final Date accidentDate;
    private final Date episodeDate;
    private final AccidentType accidentType;
    private final Module module;
    private final ClaimStatus claimStatus;

    public Claim(String claimNumber,
                 String claimDescription,
                 int claimAmount,
                 ClaimStatus claimStatus,
                 String internalPolicyNo,
                 Date accidentDate,
                 Date episodeDate,
                 AccidentType accidentType,
                 Module module){
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

    public String getClaimNumber() {
        return claimNumber;
    }

    public String getClaimDescription() {
        return claimDescription;
    }

    public int getClaimAmount() {
        return claimAmount;
    }

    public ClaimStatus getClaimStatus() { return claimStatus; }

    public String getInternalPolicyNo() { return internalPolicyNo; }

    public Date getAccidentDate() { return accidentDate; }

    public Date getEpisodeDate() { return episodeDate; }

    public AccidentType getAccidentType() { return accidentType; }

    public Module getModule() { return module; }
}
