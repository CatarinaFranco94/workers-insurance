package net.corda.examples.workinsurance.flows.models;

import net.corda.core.serialization.CordaSerializable;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.Module;

import java.util.Date;

@CordaSerializable
public class ClaimInfo {
    private final String claimNumber;
    private final String claimDescription;
    private final int claimAmount;
    private final String internalPolicyNo;
    private final Date accidentDate;
    private final Date episodeDate;
    private final AccidentType accidentType;
    private final Module module;

    public ClaimInfo(String claimNumber,
                     String claimDescription,
                     int claimAmount,
                     String internalPolicyNo,
                     Date accidentDate,
                     Date episodeDate,
                     AccidentType accidentType,
                     Module module){
        this.claimNumber = claimNumber;
        this.claimDescription = claimDescription;
        this.claimAmount = claimAmount;
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

    public String getInternalPolicyNo() {
        return internalPolicyNo;
    }

    public Date getAccidentDate() {
        return accidentDate;
    }

    public Date getEpisodeDate() {
        return episodeDate;
    }

    public AccidentType getAccidentType() {
        return accidentType;
    }

    public Module getModule() {
        return module;
    }
}
