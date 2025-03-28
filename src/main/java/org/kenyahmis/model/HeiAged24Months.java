package org.kenyahmis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "hei_aged_24_months")
public class HeiAged24Months {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String heiId;
    @JsonBackReference
    @OneToOne
    private Event event;
}
