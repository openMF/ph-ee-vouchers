package org.mifos.pheevouchermanagementsystem.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "error")
public class ErrorTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_id", nullable = false)
    private String requestId;
    @Column(name = "instruction_id", nullable = false)
    private String instructionId;
    @Column(name = "error_description", nullable = false)
    private String errorDescription;

    public ErrorTracking(String requestId, String instructionId, String errorDescription) {
        this.requestId = requestId;
        this.instructionId = instructionId;
        this.errorDescription = errorDescription;
    }
}
