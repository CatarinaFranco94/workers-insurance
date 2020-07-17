package net.corda.examples.workinsurance.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * JPA Entity for saving worker details to the database table
 */
@Entity
@Table(name = "WORKER_DETAIL")
public class PersistentWorker {

    @Id private final UUID id;
    @Column private final String policyNumber;
    @Column private final String workerName;
    @Column private final String healthNumber;

    /**
     * Default constructor required by Hibernate
     */
    public PersistentWorker() {
        this.id = null;
        this.policyNumber = null;
        this.workerName = null;
        this.healthNumber = null;
    }

    public PersistentWorker(String policyNumber, String workerName, String healthNumber) {
        this.id = UUID.randomUUID();
        this.policyNumber = policyNumber;
        this.workerName = workerName;
        this.healthNumber = healthNumber;
    }

    public UUID getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getHealthNumber() {
        return healthNumber;
    }

}
