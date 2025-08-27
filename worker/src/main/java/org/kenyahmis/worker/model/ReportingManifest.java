package org.kenyahmis.worker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "reporting_manifest")
public class ReportingManifest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String mflCode;
    private LocalDate reportDate;
}
