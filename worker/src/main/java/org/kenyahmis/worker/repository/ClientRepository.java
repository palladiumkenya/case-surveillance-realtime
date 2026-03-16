package org.kenyahmis.worker.repository;

import jakarta.transaction.Transactional;
import org.kenyahmis.worker.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    @Query("select c from Client c left join Event e on e.client = c" +
            " where c.patientPk = ?1 and e.mflCode = ?2")
    Optional<Client> findByPatientPkAndSiteCode(String patientPk, String mflCode);

    @Transactional
    @Modifying
    @Query(value = """
            update client c set	mfl_code = e.mfl_code
            from event e
            where c.mfl_code is null
            and e.client_id = c.id
            """, nativeQuery = true)
    int backfillMflCode();
    List<Client> findByMflCodeIsNull();
}
