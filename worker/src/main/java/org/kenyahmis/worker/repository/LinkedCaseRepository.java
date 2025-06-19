package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.LinkedCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkedCaseRepository extends JpaRepository<LinkedCase, UUID> {
}
