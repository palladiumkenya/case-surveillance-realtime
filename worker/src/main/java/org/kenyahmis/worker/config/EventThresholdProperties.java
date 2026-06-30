package org.kenyahmis.worker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Start-date thresholds for event processing, bound from {@code event.threshold.*}
 * properties (ISO {@code yyyy-MM-dd}). Events created before the relevant threshold are skipped.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "event.threshold")
public class EventThresholdProperties {
    private LocalDate global;
    private LocalDate prep;
    private LocalDate vl;
    private LocalDate hei;

    public LocalDateTime globalStart() {
        return global.atStartOfDay();
    }

    public LocalDateTime prepStart() {
        return prep.atStartOfDay();
    }

    public LocalDateTime vlStart() {
        return vl.atStartOfDay();
    }

    public LocalDateTime heiStart() {
        return hei.atStartOfDay();
    }
}
