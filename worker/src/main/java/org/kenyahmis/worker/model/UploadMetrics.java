package org.kenyahmis.worker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "upload_metrics")
public class UploadMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String siteCode;
    private String eventType;
    private Long recordCount;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
}
