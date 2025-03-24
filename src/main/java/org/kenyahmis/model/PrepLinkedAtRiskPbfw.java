package org.kenyahmis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "prep_linked_at_risk_pbfw")
public class PrepLinkedAtRiskPbfw {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @JsonBackReference
    @OneToOne
    private Event event;
    private LocalDateTime prepStartDate;
    private String prepRegimen;
    private String prepNumber;
}
