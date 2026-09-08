package com.epam.java.specialization.gym_crm.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "PostgreSQL is reachable")
                        .withDetail("validationQuery", "SELECT 1")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "PostgreSQL returned unexpected response")
                        .build();
            }
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("database", "PostgreSQL connection failed")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}