package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "prep_uptake")
public class PrepUptake {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @JsonBackReference
    @OneToOne
    private Event event;
    private String prepNumber;
    private String prepStatus;
    private String prepType;
    private String prepRegimen;
    private String dosingStrategy;
    private LocalDateTime prepStartDate;
    private String reasonForStartingPrep;
    private String reasonForSwitchingPrep;
    private LocalDate dateDiscontinuedFromPrep;
    private String prepDiscontinuationReason;
    private LocalDate dateSwitchedPrep;
    private String isPregnant;
    private String isBreastfeeding;
}
