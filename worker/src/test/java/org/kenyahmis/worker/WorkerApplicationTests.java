package org.kenyahmis.worker;

import org.junit.jupiter.api.Test;
import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EmrVendorRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.kenyahmis.worker.repository.LinkedCaseRepository;
import org.kenyahmis.worker.repository.NewCaseEventRepository;
import org.kenyahmis.worker.repository.ReportingManifestRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
        "KAFKA_BOOTSTRAP_SERVERS=localhost:9092",
        "DATABASE_URL=jdbc:postgresql://localhost:5432/test",
        "DATABASE_USER=test",
        "DATABASE_PASSWORD=test"
})
class WorkerApplicationTests {

    @MockBean EventRepository eventRepository;
    @MockBean ClientRepository clientRepository;
    @MockBean EmrVendorRepository emrVendorRepository;
    @MockBean LinkedCaseRepository linkedCaseRepository;
    @MockBean NewCaseEventRepository newCaseEventRepository;
    @MockBean ReportingManifestRepository reportingManifestRepository;

    @Test
    void contextLoads() {
    }

}
