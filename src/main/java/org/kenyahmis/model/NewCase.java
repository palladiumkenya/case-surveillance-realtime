package org.kenyahmis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "new_case")
public class NewCase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private Event event;
    private LocalDateTime positiveHivTestDate;
}
