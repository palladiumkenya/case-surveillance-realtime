package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.Event;
import org.kenyahmis.worker.model.EventMigrationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventType(String patientPk, String mflCode, String eventType);
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventTypeAndEligibleForVl_VisitDate(
            String patientPk, String mflCode, String eventType, LocalDateTime visitDate);
    Optional<Event> findByEventUniqueId(String eventUniqueId);
//    List<Event> findByEventUniqueIdIsNull(Pageable pageable);
    List<Event> findByClient(Client client);
    @Query("select new org.kenyahmis.worker.model.EventMigrationDto(e, c.patientPk) from Event e" +
            " left join Client  c on c.id = e.client.id")
    List<EventMigrationDto> findByEventUniqueIdIsNull(Pageable pageable);
}
