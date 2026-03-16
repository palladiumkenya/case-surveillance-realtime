package org.kenyahmis.worker.model;


public record EventMigrationDto(
        Event event,
        String patientPk
) {
}
