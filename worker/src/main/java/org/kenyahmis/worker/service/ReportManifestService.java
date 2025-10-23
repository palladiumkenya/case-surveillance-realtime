package org.kenyahmis.worker.service;

import jakarta.transaction.Transactional;
import org.kenyahmis.shared.dto.ManifestMessage;
import org.kenyahmis.worker.model.ReportingManifest;
import org.kenyahmis.worker.repository.ReportingManifestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReportManifestService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final ReportingManifestRepository reportingManifestRepository;

    public ReportManifestService(ReportingManifestRepository reportingManifestRepository) {
        this.reportingManifestRepository = reportingManifestRepository;
    }

    @KafkaListener(id = "manifestListener", topics = "reporting_manifest", containerFactory = "manifestKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void createManifest(ManifestMessage manifestMessage) {
        LocalDate reportDate = LocalDate.now();
        for (String mflCode: manifestMessage.getMflCodes()) {
            Optional<ReportingManifest> optionalReportingManifest =  reportingManifestRepository.findByMflCodeAndReportDate(mflCode, reportDate);
            if (optionalReportingManifest.isEmpty()) {
                // add manifest
                LOG.info("Adding manifest for {} on {}", mflCode, reportDate);
                ReportingManifest reportingManifest = new ReportingManifest();
                reportingManifest.setReportDate(reportDate);
                reportingManifest.setMflCode(mflCode);
                reportingManifest.setEmrVersion(manifestMessage.getEmrVersion());
                reportingManifestRepository.save(reportingManifest);
            }
        }
    }
}
