package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "missed_vl_opportunities")
public class MissedVlOpportunities {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @OneToOne
    @JsonBackReference
    private Event event;
    private LocalDateTime visitDate;
    private Boolean missedVlFlag;
}
