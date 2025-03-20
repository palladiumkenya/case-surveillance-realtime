package org.kenyahmis.repository;

import org.kenyahmis.model.LinkedCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkedCaseRepository extends JpaRepository<LinkedCase, UUID> {
}
