package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "mortality")
@Entity
public class Mortality {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String causeOfDeath;
    @JsonBackReference
    @OneToOne
    private Event event;
    private LocalDateTime deathDate;
}
