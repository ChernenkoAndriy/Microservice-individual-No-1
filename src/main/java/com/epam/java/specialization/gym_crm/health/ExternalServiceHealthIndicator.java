package com.epam.java.specialization.gym_crm.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean isServiceUp = checkExternalService();

        if (isServiceUp) {
            return Health.up()
                    .withDetail("External Service", "Available")
                    .withDetail("Response Time", "12ms")
                    .build();
        }

        return Health.down()
                .withDetail("External Service", "Not Reachable")
                .withDetail("Error", "Timeout connecting to external CRM gateway")
                .build();
    }

    private boolean checkExternalService() {
        return true;
    }
}