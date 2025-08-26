package org.kenyahmis.worker.repository;

import org.kenyahmis.worker.model.EmrVendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmrVendorRepository extends JpaRepository<EmrVendor, UUID> {
    Optional<EmrVendor> findByVendorName(String vendorName);
}
