package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingMapperTest {

    private TrainingMapper trainingMapper;

    @BeforeEach
    void setUp() {
        trainingMapper = new TrainingMapperImpl();
    }

    @Test
    @DisplayName("Should map Training to Trainee report and concatenate Trainer full name")
    void toTraineeReportResponse_ShouldConcatenateTrainerName() {
        User trainerUser = User.builder().firstName("Vitaliy").lastName("Klychko").build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();

        TrainingType type = TrainingType.builder().trainingTypeName("Heavyweight").build();

        Training training = Training.builder()
                .trainingName("Title Fight")
                .trainingDate(new Date())
                .trainingDuration(12)
                .trainer(trainer)
                .trainingType(type)
                .build();

        TraineeTrainingResponseDto response = trainingMapper.toTraineeReportResponse(training);

        assertThat(response.getTrainingName()).isEqualTo("Title Fight");
        assertThat(response.getTrainingType()).isEqualTo("Heavyweight");
        assertThat(response.getTrainerName()).isEqualTo("Vitaliy Klychko");
    }

    @Test
    @DisplayName("Should map Training to Trainer report and concatenate Trainee full name")
    void toTrainerReportResponse_ShouldConcatenateTraineeName() {
        User traineeUser = User.builder().firstName("John").lastName("Cena").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();

        TrainingType type = TrainingType.builder().trainingTypeName("Wrestling").build();

        Training training = Training.builder()
                .trainingName("Showtime")
                .trainee(trainee)
                .trainingType(type)
                .trainingDuration(45)
                .build();

        TrainerTrainingResponseDto response = trainingMapper.toTrainerReportResponse(training);

        assertThat(response.getTraineeName()).isEqualTo("John Cena");
        assertThat(response.getTrainingType()).isEqualTo("Wrestling");
    }
}