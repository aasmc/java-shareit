package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.shareit.testutil.ShareItPostgresContainer;

@ActiveProfiles("integtest")
@DataJpaTest
@Testcontainers
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BaseJpaTest {

    @Container
    static PostgreSQLContainer<ShareItPostgresContainer> container = ShareItPostgresContainer.getInstance();

}
