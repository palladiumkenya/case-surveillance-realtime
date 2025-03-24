package org.kenyahmis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "unsuppressed_viral_load")
public class UnsuppressedViralLoad {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @OneToOne
    @JsonBackReference
    private Event event;
    private LocalDateTime positiveHivTestDate;
    private LocalDateTime visitDate;
    private LocalDateTime artStartDate;
    private String pregnancyStatus;
    private String breastFeedingStatus;
    private String lastVlResults;
    private LocalDateTime lastVlOrderDate;
    private LocalDateTime lastVlResultsDate;
    private LocalDateTime lastEacEncounterDate;
}
