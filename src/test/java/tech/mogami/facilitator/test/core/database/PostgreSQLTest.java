package tech.mogami.facilitator.test.core.database;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.mogami.facilitator.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16:///explorer",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@DisplayName("PostgresSQL database tests")
public class PostgreSQLTest {

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    @DisplayName("Database schema initialization")
    void databaseSchemaInitialization() {
        assertThatCode(() -> paymentRepository.count())
                .doesNotThrowAnyException();
    }

}
