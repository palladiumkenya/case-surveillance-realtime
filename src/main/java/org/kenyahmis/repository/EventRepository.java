package org.kenyahmis.repository;

import org.kenyahmis.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventType(String patientPk, String mflCode, String eventType);
}
