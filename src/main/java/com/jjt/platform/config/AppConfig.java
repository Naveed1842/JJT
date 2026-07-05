package com.jjt.platform.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {

    /**
     * Repair Flyway schema history before migrating.
     * This removes any failed migration records so that corrected scripts can be re-applied
     * without requiring manual intervention in the flyway_schema_history table.
     * Safe to run on every startup: repair is a no-op when there are no failed entries.
     */
    @Bean
    public FlywayMigrationStrategy repairAndMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
