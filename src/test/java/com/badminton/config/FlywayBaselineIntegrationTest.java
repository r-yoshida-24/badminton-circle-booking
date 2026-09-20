package com.badminton.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayBaselineIntegrationTest {

    @Test
    void emptySchemaStillAppliesAllMigrations() throws SQLException {
        String url = jdbcUrl("flyway-empty");

        Flyway flyway = configuredFlyway(url);
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertThat(tableExists(connection, "members")).isTrue();
            assertThat(tableExists(connection, "events")).isTrue();
            assertThat(tableExists(connection, "attendances")).isTrue();
            assertThat(versionedHistoryEntries(connection)).containsExactly(
                    new HistoryEntry("1", "SQL"),
                    new HistoryEntry("2", "SQL"),
                    new HistoryEntry("3", "SQL")
            );
        }
    }

    @Test
    void nonEmptySchemaWithoutHistoryBaselinesAtV1AndAppliesLaterMigrations() throws SQLException {
        String url = jdbcUrl("flyway-baseline");

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE members (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        line_user_id VARCHAR(191) NOT NULL,
                        display_name VARCHAR(100),
                        role VARCHAR(20) NOT NULL,
                        enabled BOOLEAN NOT NULL,
                        version BIGINT NOT NULL DEFAULT 0,
                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,
                        CONSTRAINT uk_members_line_user_id UNIQUE (line_user_id),
                        CONSTRAINT ck_members_role CHECK (role IN ('USER', 'ADMIN'))
                    )
                    """);
        }

        Flyway flyway = configuredFlyway(url);
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertThat(tableExists(connection, "flyway_schema_history")).isTrue();
            assertThat(tableExists(connection, "members")).isTrue();
            assertThat(tableExists(connection, "events")).isTrue();
            assertThat(tableExists(connection, "attendances")).isTrue();
            assertThat(versionedHistoryEntries(connection)).containsExactly(
                    new HistoryEntry("1", "BASELINE"),
                    new HistoryEntry("2", "SQL"),
                    new HistoryEntry("3", "SQL")
            );
        }
    }

    private Flyway configuredFlyway(String url) {
        FlywayProperties flywayProperties = loadAdoptionFlywayProperties();
        return Flyway.configure()
                .dataSource(url, "sa", "")
                .locations(flywayProperties.getLocations().toArray(String[]::new))
                .baselineOnMigrate(Boolean.TRUE.equals(flywayProperties.isBaselineOnMigrate()))
                .baselineVersion(MigrationVersion.fromVersion(flywayProperties.getBaselineVersion()))
                .load();
    }

    private FlywayProperties loadAdoptionFlywayProperties() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(FlywayPropertiesTestConfig.class)
                .profiles("flyway-adopt-existing-schema")
                .properties("spring.config.location=" + mainApplicationConfigLocation())
                .web(WebApplicationType.NONE)
                .run()) {
            return context.getBean(FlywayProperties.class);
        }
    }

    private String mainApplicationConfigLocation() {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("application.yml");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (!resource.toString().contains("/test-classes/")) {
                    return resource.toString();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to locate main application.yml on the classpath", e);
        }
        throw new IllegalStateException("Main application.yml was not found on the classpath");
    }

    private String jdbcUrl(String name) {
        return "jdbc:h2:mem:" + name + "-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private List<HistoryEntry> versionedHistoryEntries(Connection connection) throws SQLException {
        List<HistoryEntry> entries = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT version, type FROM flyway_schema_history WHERE success = TRUE AND version IS NOT NULL ORDER BY installed_rank");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                entries.add(new HistoryEntry(resultSet.getString("version"), resultSet.getString("type")));
            }
        }
        return entries;
    }

    private record HistoryEntry(String version, String type) {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(FlywayProperties.class)
    static class FlywayPropertiesTestConfig {
    }
}
