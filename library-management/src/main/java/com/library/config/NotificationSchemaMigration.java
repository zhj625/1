package com.library.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            migrateNotificationTypeColumn();
        } catch (Exception e) {
            log.error("Failed to migrate notification.type column", e);
        }
    }

    private void migrateNotificationTypeColumn() {
        String sql = """
                SELECT DATA_TYPE, COLUMN_TYPE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'notification'
                  AND COLUMN_NAME = 'type'
                """;

        jdbcTemplate.query(sql, rs -> {
            String dataType = rs.getString("DATA_TYPE");
            String columnType = rs.getString("COLUMN_TYPE");

            boolean shouldMigrate = "enum".equalsIgnoreCase(dataType);
            if (columnType != null) {
                String normalized = columnType.toUpperCase(Locale.ROOT);
                shouldMigrate = shouldMigrate
                        || !normalized.contains("BOOK_AVAILABLE")
                        || !normalized.contains("RESERVATION_EXPIRED");
            }

            if (!shouldMigrate) {
                return;
            }

            log.warn("Migrating notification.type from legacy definition: {}", columnType);
            jdbcTemplate.execute("ALTER TABLE notification MODIFY COLUMN type VARCHAR(30) NOT NULL");
            log.info("notification.type migrated to VARCHAR(30)");
        });
    }
}
