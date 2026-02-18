package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventType(String patientPk, String mflCode, String eventType);
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventTypeAndEligibleForVl_VisitDate(
            String patientPk, String mflCode, String eventType, LocalDateTime visitDate);
}
