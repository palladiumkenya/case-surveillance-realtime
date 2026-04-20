package org.kenyahmis.worker.repository;

import jakarta.transaction.Transactional;
import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Deprecated
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventType(String patientPk, String mflCode, String eventType);
    @Deprecated
    Optional<Event> findByClient_PatientPkAndMflCodeAndEventTypeAndEligibleForVl_VisitDate(
            String patientPk, String mflCode, String eventType, LocalDateTime visitDate);
    Optional<Event> findByEventUniqueId(String eventUniqueId);
    List<Event> findByClient(Client client);
    @Transactional
    @Modifying
    @Query(value = """
            UPDATE event e
            SET event_unique_id = md5(concat(
                c.patient_pk,
                e.mfl_code,
                e.event_type,
                COALESCE(to_char(e.created_at, 'YYYY-MM-DD HH24:MI:SS'), '')
            ))
            FROM client c
            WHERE e.event_unique_id IS NULL
            AND e.client_id = c.id
            AND e.event_type != 'eligible_for_vl'
            """, nativeQuery = true)
    int backfillNonVlEventUniqueId();

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE event e
            SET event_unique_id = md5(concat(
                c.patient_pk,
                e.mfl_code,
                e.event_type,
                COALESCE(to_char(vl.visit_date, 'YYYY-MM-DD HH24:MI:SS'), '')
            ))
            FROM client c, eligible_for_vl vl
            WHERE e.event_unique_id IS NULL
            AND e.client_id = c.id
            AND e.event_type = 'eligible_for_vl'
            AND vl.event_id = e.id
            """, nativeQuery = true)
    int backfillVlEventUniqueId();
}
