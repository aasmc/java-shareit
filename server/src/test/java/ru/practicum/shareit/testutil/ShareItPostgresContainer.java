package ru.practicum.shareit.testutil;

import org.testcontainers.containers.PostgreSQLContainer;

public class ShareItPostgresContainer extends PostgreSQLContainer<ShareItPostgresContainer> {
    private static final String IMAGE_VERSION = "postgres:15.3";
    private static ShareItPostgresContainer container;

    private ShareItPostgresContainer() {
        super(IMAGE_VERSION);
    }

    public static ShareItPostgresContainer getInstance() {
        if (container == null) {
            container = new ShareItPostgresContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}
