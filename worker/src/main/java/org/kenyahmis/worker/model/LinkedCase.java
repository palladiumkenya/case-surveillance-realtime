package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "linked_case")
public class LinkedCase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @JsonBackReference
    @OneToOne
    private Event event;
    private LocalDateTime artStartDate;
}
