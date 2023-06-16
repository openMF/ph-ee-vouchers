package org.mifos.pheevouchermanagementsystem.domain;

import javax.persistence.*;

@Entity
@Table(name = "error_tracking")
public class ErrorTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_id", nullable = false)
    private String requestId ;
    @Column(name = "instruction_id", nullable = false)
    private String instructionId ;
    @Column(name = "error_description", nullable = false)
    private String errorDescription ;

    public ErrorTracking(String requestId, String instructionId, String errorDescription) {
        this.requestId = requestId;
        this.instructionId = instructionId;
        this.errorDescription = errorDescription;
    }
}
