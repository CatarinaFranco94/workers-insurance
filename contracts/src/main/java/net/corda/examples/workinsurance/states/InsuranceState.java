package net.corda.examples.workinsurance.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.examples.workinsurance.contracts.InsuranceContract;
import net.corda.examples.workinsurance.schema.InsuranceSchemaV1;
import net.corda.examples.workinsurance.schema.PersistentClaim;
import net.corda.examples.workinsurance.schema.PersistentInsurance;
import net.corda.examples.workinsurance.schema.PersistentWorker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Insurance State
 * The state should implement the QueryableState to support custom schema development.
 */
@BelongsToContract(InsuranceContract.class)
public class InsuranceState implements QueryableState {

    // Represents the asset which is insured.
    // This will be used to demonstrate one-to-one relationship
    private final WorkerDetail workerDetail;

    // Fields related to the insurance state.
    private final long insuredValue;
    private final int duration;

    private final Party insurer;
    private final Party insuree;

    // Insurance claims made against the insurace policy
    // This will be used to demonstrate one-to-many relationship
    private final List<Claim> claims;

    public InsuranceState(long insuredValue, int duration, Party insurer,
                          Party insuree, WorkerDetail workerDetail, List<Claim> claims) {
        this.insuredValue = insuredValue;
        this.duration = duration;
        this.insurer = insurer;
        this.insuree = insuree;
        this.workerDetail = workerDetail;
        this.claims = claims;
    }

    /**
     * Used to Generate the Entity for this Queryable State.
     * This method is called by the SchemaService of the node, and the returned entity is handed over to the ORM tool
     * to be persisted in custom database table.
     *
     * @param schema
     * @return PersistentState
     */
    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if(schema instanceof InsuranceSchemaV1){

            // Create list of PersistentClaim entity against every Claims object.
            List<PersistentClaim> persistentClaims = new ArrayList<>();
            if(claims != null && claims.size() > 0) {
                for(Claim claim: claims){
                    PersistentClaim persistentClaim = new PersistentClaim(
                            claim.getClaimNumber(),
                            claim.getClaimDescription(),
                            claim.getClaimAmount(),
                            claim.getClaimStatus()
                    );
                    persistentClaims.add(persistentClaim);
                }
            }

            return new PersistentInsurance(
                    this.insuredValue,
                    this.duration,
                    this.workerDetail ==null ? null : new PersistentWorker(
                            workerDetail.getPolicyNumber(),
                            workerDetail.getName(),
                            workerDetail.getHealthNumber(),
                            workerDetail.getPolicyHolder()
                    ),
                    this.claims == null? null: persistentClaims
            );
        }else{
            throw new IllegalArgumentException("Unsupported Schema");
        }
    }

    /**
     * Returns a list of supported Schemas by this Queryable State.
     *
     * @return Iterable<MappedSchema>
     */
    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new InsuranceSchemaV1());
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(insuree, insurer);
    }

    public long getInsuredValue() {
        return insuredValue;
    }

    public int getDuration() {
        return duration;
    }

    public Party getInsurer() {
        return insurer;
    }

    public Party getInsuree() {
        return insuree;
    }

    public WorkerDetail getWorkerDetail() {
        return workerDetail;
    }

    public List<Claim> getClaims() {
        return claims;
    }
}
