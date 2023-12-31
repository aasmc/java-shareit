package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.shareit.testutil.ShareItPostgresContainer;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("integtest")
@Sql(
        scripts = "classpath:db/clear-db.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class BaseIntegTest {

    @Container
    static PostgreSQLContainer container = ShareItPostgresContainer.getInstance();

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

}
