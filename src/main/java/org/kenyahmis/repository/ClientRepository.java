package org.kenyahmis.repository;

import org.kenyahmis.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    @Query("select c from Client c left join Event e on e.client = c" +
            " where c.patientPk = ?1 and e.mflCode = ?2")
    Optional<Client> findByPatientPkAndSiteCode(String patientPk, String mflCode);
}
