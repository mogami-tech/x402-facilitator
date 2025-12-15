package tech.mogami.facilitator.test.database;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16:///explorer",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@DisplayName("PostgresSQL database tests")
public class PostgreSQLTest {

    @Test
    @DisplayName("Database schema initialization")
    void databaseSchemaInitialization() {
        // If the context loads successfully, the schema has been initialized correctly.
    }

}
