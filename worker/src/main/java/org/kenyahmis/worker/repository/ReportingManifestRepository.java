package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.ReportingManifest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ReportingManifestRepository extends JpaRepository<ReportingManifest, UUID> {
    Optional<ReportingManifest> findByMflCodeAndReportDate(String mflCode, LocalDate reportDate);
}
