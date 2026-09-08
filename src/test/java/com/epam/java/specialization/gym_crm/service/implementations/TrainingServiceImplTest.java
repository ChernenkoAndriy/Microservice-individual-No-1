package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.exception.InactiveUserException;
import com.epam.java.specialization.gym_crm.mapper.TrainingMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.*;
import com.epam.java.specialization.gym_crm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import io.micrometer.core.instrument.Timer;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private CrmMetrics crmMetrics;
    @Mock
    private Timer timer;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        lenient().when(crmMetrics.getTrainingCreationTimer()).thenReturn(timer);
        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(timer).record(any(Runnable.class));
    }

    @Test
    @DisplayName("Should successfully retrieve filtered trainee trainings when trainee exists")
    @SuppressWarnings("unchecked")
    void getTraineeTrainings_ShouldSearchAndReturnMappedList() {
        String username = "Trainee.One";
        Trainee trainee = Trainee.builder().id(1L).build();
        List<Training> trainingsList = Collections.singletonList(Training.builder().build());
        List<TraineeTrainingResponseDto> responseDtos = Collections.singletonList(new TraineeTrainingResponseDto());

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(trainingsList);
        when(trainingMapper.toTraineeReportResponseList(trainingsList)).thenReturn(responseDtos);

        List<TraineeTrainingResponseDto> result = trainingService.getTraineeTrainings(
                username, null, null, null, null);

        assertThat(result).hasSize(1);
        verify(trainingRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if seeking trainings of non-existing trainee")
    void getTraineeTrainings_ShouldThrowException_WhenTraineeDoesNotExist() {
        String username = "Missing.Trainee";
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTraineeTrainings(username, null, null, null, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found with username: Missing.Trainee");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if seeking trainings of non-existing trainer")
    void getTrainerTrainings_ShouldThrowException_WhenTrainerDoesNotExist() {
        String username = "Missing.Trainer";
        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTrainerTrainings(username, null, null, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found with username: Missing.Trainer");
    }

    @Test
    @DisplayName("Should add new training, link trainer to trainee, and save relationship if not linked yet")
    void addTraining_ShouldRegisterTrainingAndEstablishTrainerTraineeLink_WhenBothUsersAreActive() {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Trainee.User")
                .trainerUsername("Trainer.User")
                .trainingName("Yoga Intro")
                .trainingDate(new Date())
                .trainingDuration(45)
                .build();

        User traineeUser = User.builder().username("Trainee.User").isActive(true).build();
        Trainee trainee = Trainee.builder().user(traineeUser).trainers(new ArrayList<>()).build();

        TrainingType spec = TrainingType.builder().trainingTypeName("Yoga").build();
        User trainerUser = User.builder().username("Trainer.User").isActive(true).build();
        Trainer trainer = Trainer.builder().user(trainerUser).specialization(spec).build();

        when(traineeRepository.findByUserUsername("Trainee.User")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername("Trainer.User")).thenReturn(Optional.of(trainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainingService.addTraining(request);

        assertThat(trainee.getTrainers()).contains(trainer);
        verify(traineeRepository, times(1)).save(trainee);
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    @DisplayName("Should throw InactiveUserException if trainee is inactive during add training process")
    void addTraining_ShouldThrowInactiveUserException_WhenTraineeIsInactive() {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Inactive.Trainee")
                .trainerUsername("Active.Trainer")
                .build();

        Trainee inactiveTrainee = Trainee.builder()
                .user(User.builder().username("Inactive.Trainee").isActive(false).build())
                .build();

        Trainer activeTrainer = Trainer.builder()
                .user(User.builder().username("Active.Trainer").isActive(true).build())
                .build();

        when(traineeRepository.findByUserUsername("Inactive.Trainee")).thenReturn(Optional.of(inactiveTrainee));
        when(trainerRepository.findByUserUsername("Active.Trainer")).thenReturn(Optional.of(activeTrainer));

        assertThatThrownBy(() -> trainingService.addTraining(request))
                .isInstanceOf(InactiveUserException.class)
                .hasMessageContaining("Cannot add training: Trainee profile is inactive.");

        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    @DisplayName("Should throw InactiveUserException if trainer is inactive during add training process")
    void addTraining_ShouldThrowInactiveUserException_WhenTrainerIsInactive() {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Active.Trainee")
                .trainerUsername("Inactive.Trainer")
                .build();

        Trainee activeTrainee = Trainee.builder()
                .user(User.builder().username("Active.Trainee").isActive(true).build())
                .build();

        Trainer inactiveTrainer = Trainer.builder()
                .user(User.builder().username("Inactive.Trainer").isActive(false).build())
                .build();

        when(traineeRepository.findByUserUsername("Active.Trainee")).thenReturn(Optional.of(activeTrainee));
        when(trainerRepository.findByUserUsername("Inactive.Trainer")).thenReturn(Optional.of(inactiveTrainer));

        assertThatThrownBy(() -> trainingService.addTraining(request))
                .isInstanceOf(InactiveUserException.class)
                .hasMessageContaining("Cannot add training: Trainer profile is inactive.");

        verify(trainingRepository, never()).save(any(Training.class));
    }
}