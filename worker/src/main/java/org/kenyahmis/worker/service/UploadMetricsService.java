package org.kenyahmis.worker.service;

import jakarta.transaction.Transactional;
import org.kenyahmis.shared.dto.UploadMetricsMessage;
import org.kenyahmis.worker.model.UploadMetrics;
import org.kenyahmis.worker.repository.UploadMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploadMetricsService {
    private static final Logger LOG = LoggerFactory.getLogger(UploadMetricsService.class);
    private final UploadMetricsRepository uploadMetricsRepository;

    public UploadMetricsService(UploadMetricsRepository uploadMetricsRepository) {
        this.uploadMetricsRepository = uploadMetricsRepository;
    }

    @KafkaListener(id = "uploadMetricsListener", topics = "upload_metrics", containerFactory = "uploadMetricsKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void recordMetrics(List<UploadMetricsMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<UploadMetrics> rows = new ArrayList<>(messages.size());
        for (UploadMetricsMessage msg : messages) {
            UploadMetrics row = new UploadMetrics();
            row.setSiteCode(msg.getSiteCode());
            row.setEventType(msg.getEventType());
            row.setRecordCount(msg.getCount());
            row.setTimestamp(msg.getTimestamp() == null
                    ? now
                    : LocalDateTime.ofInstant(msg.getTimestamp(), ZoneOffset.systemDefault()));
            row.setCreatedAt(now);
            rows.add(row);
        }
        uploadMetricsRepository.saveAll(rows);
        LOG.info("Persisted {} upload metrics rows", rows.size());
    }
}
