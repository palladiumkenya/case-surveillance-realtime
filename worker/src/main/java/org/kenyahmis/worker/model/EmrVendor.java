package org.kenyahmis.worker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "emr_vendor")
public class EmrVendor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String vendorName;
}
