package org.kenyahmis.repository;

import org.kenyahmis.model.NewCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewCaseEventRepository extends JpaRepository<NewCase, UUID> {
}
