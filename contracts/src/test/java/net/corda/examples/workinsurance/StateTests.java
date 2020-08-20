package net.corda.examples.workinsurance;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.examples.workinsurance.enums.AccidentType;
import net.corda.examples.workinsurance.enums.ClaimStatus;
import net.corda.examples.workinsurance.enums.Module;
import net.corda.examples.workinsurance.states.Claim;
import net.corda.examples.workinsurance.states.InsuranceDetail;
import net.corda.examples.workinsurance.states.InsuranceState;
import net.corda.examples.workinsurance.states.WorkerDetail;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class StateTests {
    private final Party hospitalSaoJoao = new TestIdentity(new CordaX500Name("HSJ", "", "GB")).getParty();
    private final Party agsInsurance = new TestIdentity(new CordaX500Name("AGS", "","GB")).getParty();
    private final WorkerDetail workerDetail = new WorkerDetail("policyNr", "Antonio", "123456", "CSW");

    @Test
    public void insuranceStateHasAllPropertiesOfCorrectTypeInConstructor(){
        new InsuranceState(10, 12, agsInsurance, hospitalSaoJoao, workerDetail, Collections.emptyList());
    }

    @Test
    public void insuranceStateHasGettersForAllProperties(){
        InsuranceState insuranceState = new InsuranceState(5, 24, agsInsurance, hospitalSaoJoao, workerDetail, Collections.emptyList());
        assertEquals(5, insuranceState.getInsuredValue());
        assertEquals(24, insuranceState.getDuration());
        assertEquals(agsInsurance, insuranceState.getInsurer());
        assertEquals(hospitalSaoJoao, insuranceState.getInsuree());
        assertTrue(workerDetail.equals(insuranceState.getWorkerDetail()));
        assertEquals(0, insuranceState.getClaims().size());
    }

    @Test
    public void insuranceStateHasClaims(){
        Date accidentDate = new Date(2019,10,12);
        Claim claim = new Claim("N1", "Minor accident", 200, ClaimStatus.Proposal,
                "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
                null, hospitalSaoJoao, agsInsurance);

        InsuranceState insuranceState  = new InsuranceState(2000, 12, agsInsurance, hospitalSaoJoao, workerDetail, Arrays.asList(claim));

        assertEquals(1, insuranceState.getClaims().size());
        assertEquals(claim.getClaimNumber(), insuranceState.getClaims().get(0).getClaimNumber());
        assertEquals(claim.getClaimDescription(), insuranceState.getClaims().get(0).getClaimDescription());
        assertEquals(claim.getClaimAmount(), insuranceState.getClaims().get(0).getClaimAmount());
        assertEquals(claim.getClaimStatus(), insuranceState.getClaims().get(0).getClaimStatus());
        assertEquals(claim.getInternalPolicyNo(), insuranceState.getClaims().get(0).getInternalPolicyNo());
        assertEquals(claim.getAccidentDate(), insuranceState.getClaims().get(0).getAccidentDate());
        assertEquals(claim.getEpisodeDate(), insuranceState.getClaims().get(0).getEpisodeDate());
        assertEquals(claim.getAccidentType(), insuranceState.getClaims().get(0).getAccidentType());
        assertEquals(claim.getModule(), insuranceState.getClaims().get(0).getModule());
        assertEquals(claim.getInsuranceDetail(), insuranceState.getClaims().get(0).getInsuranceDetail());
        assertEquals(claim.getProposee(), insuranceState.getClaims().get(0).getProposee());
        assertEquals(claim.getProposer(), insuranceState.getClaims().get(0).getProposer());
    }

    @Test
    public void insuranceStateHasClaimsAcceptedWithInsruanceDetail() {
        Date accidentDate = new Date(2019, 10, 12);

        InsuranceDetail insuranceDetail = new InsuranceDetail("InsuranceCompanyNr", "InsuranceCompanyPolicyNr",
                "Field");

        Claim claim = new Claim("N1", "Minor accident", 200, ClaimStatus.Accepted,
                "internalPolicyNr", accidentDate, accidentDate, AccidentType.WorkAccident, Module.DayHospital,
                insuranceDetail, hospitalSaoJoao, agsInsurance);

        InsuranceState insuranceState = new InsuranceState(2000, 12, agsInsurance, hospitalSaoJoao, workerDetail, Arrays.asList(claim));

        assertEquals(1, insuranceState.getClaims().size());
        assertEquals(claim.getClaimStatus(), insuranceState.getClaims().get(0).getClaimStatus());
        assertEquals(claim.getInsuranceDetail().getField(), insuranceState.getClaims().get(0).getInsuranceDetail().getField());
        assertEquals(claim.getInsuranceDetail().getInsuranceCompanyNumber(), insuranceState.getClaims().get(0).getInsuranceDetail().getInsuranceCompanyNumber());
        assertEquals(claim.getInsuranceDetail().getInsuranceCompanyPolicyNumber(), insuranceState.getClaims().get(0).getInsuranceDetail().getInsuranceCompanyPolicyNumber());
    }

        @Test
    public void insuranceStateImplementsContractState(){
        assertTrue(new InsuranceState(10, 2, agsInsurance, hospitalSaoJoao, workerDetail, Collections.emptyList()) instanceof ContractState);
    }

    @Test
    public void insuranceStateHasTwoParticipantsTheIssuerAndTheInsuree(){
        InsuranceState insuranceState = new InsuranceState(2000, 10, agsInsurance, hospitalSaoJoao, workerDetail, Collections.emptyList());
        assertEquals(2, insuranceState.getParticipants().size());
        assertTrue(insuranceState.getParticipants().contains(agsInsurance));
        assertTrue(insuranceState.getParticipants().contains(hospitalSaoJoao));
    }


}
