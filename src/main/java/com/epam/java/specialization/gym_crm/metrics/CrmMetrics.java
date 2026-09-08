package com.epam.java.specialization.gym_crm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CrmMetrics {

    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Timer trainingCreationTimer;

    public CrmMetrics(MeterRegistry registry) {
        this.traineeRegistrationCounter = Counter.builder("crm.user.registrations")
                .tag("role", "trainee")
                .description("Total number of registered trainees")
                .register(registry);

        this.trainerRegistrationCounter = Counter.builder("crm.user.registrations")
                .tag("role", "trainer")
                .description("Total number of registered trainers")
                .register(registry);

        this.trainingCreationTimer = Timer.builder("crm.training.creation.time")
                .description("Time taken to create a new training session")
                .register(registry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrationCounter.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrationCounter.increment();
    }

    public Timer getTrainingCreationTimer() {
        return trainingCreationTimer;
    }
}