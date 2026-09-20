package com.badminton.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayBaselineIntegrationTest {

    @Test
    void emptySchemaStillAppliesAllMigrations() throws SQLException {
        String url = jdbcUrl("flyway-empty");

        Flyway flyway = configuredFlyway(url);
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            assertThat(tableExists(statement, "members")).isTrue();
            assertThat(tableExists(statement, "events")).isTrue();
            assertThat(tableExists(statement, "attendances")).isTrue();
            assertThat(appliedVersions(statement)).containsExactly("1", "2", "3");
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

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            assertThat(tableExists(statement, "flyway_schema_history")).isTrue();
            assertThat(tableExists(statement, "members")).isTrue();
            assertThat(tableExists(statement, "events")).isTrue();
            assertThat(tableExists(statement, "attendances")).isTrue();
            assertThat(appliedVersions(statement)).containsExactly("1", "2", "3");
            assertThat(historyTypeForVersion(statement, "1")).isEqualTo("BASELINE");
        }
    }

    private Flyway configuredFlyway(String url) {
        return Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1"))
                .load();
    }

    private String jdbcUrl(String name) {
        return "jdbc:h2:mem:" + name + "-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    }

    private boolean tableExists(Statement statement, String tableName) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = '" + tableName + "'")) {
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }
    }

    private List<String> appliedVersions(Statement statement) throws SQLException {
        List<String> versions = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT version FROM flyway_schema_history WHERE success = TRUE AND version IS NOT NULL ORDER BY installed_rank")) {
            while (resultSet.next()) {
                versions.add(resultSet.getString(1));
            }
        }
        return versions;
    }

    private String historyTypeForVersion(Statement statement, String version) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT type FROM flyway_schema_history WHERE version = '" + version + "'")) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
