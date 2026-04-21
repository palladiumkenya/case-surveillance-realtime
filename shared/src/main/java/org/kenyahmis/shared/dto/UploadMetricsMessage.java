package org.kenyahmis.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadMetricsMessage {
    private String siteCode;
    private String eventType;
    private Long count;
    private Instant timestamp;
}
