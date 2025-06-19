package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.NewCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewCaseEventRepository extends JpaRepository<NewCase, UUID> {
}
