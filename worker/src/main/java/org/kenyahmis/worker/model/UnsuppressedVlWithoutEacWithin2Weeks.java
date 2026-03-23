package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "unsuppressed_vl_without_eac_within_2_weeks")
public class UnsuppressedVlWithoutEacWithin2Weeks {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @OneToOne
    @JsonBackReference
    private Event event;
    @Column(name = "date_14_days_post_hvl")
    private LocalDateTime date14DaysPostHvl;
    private Boolean missedEacFlag;
}
