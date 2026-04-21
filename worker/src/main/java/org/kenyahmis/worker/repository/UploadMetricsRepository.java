package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.UploadMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UploadMetricsRepository extends JpaRepository<UploadMetrics, UUID> {
}
