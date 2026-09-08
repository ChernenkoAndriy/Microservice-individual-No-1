package com.epam.java.specialization.gym_crm.training.internal;

import com.epam.java.specialization.gym_crm.common.exception.InactiveUserException;
import com.epam.java.specialization.gym_crm.common.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.trainee.Trainee;
import com.epam.java.specialization.gym_crm.trainee.TraineeService;
import com.epam.java.specialization.gym_crm.common.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.trainer.Trainer;
import com.epam.java.specialization.gym_crm.trainer.TrainerService;
import com.epam.java.specialization.gym_crm.common.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.training.Training;
import com.epam.java.specialization.gym_crm.training.TrainingCreatedEvent;
import com.epam.java.specialization.gym_crm.training.TrainingService;
import com.epam.java.specialization.gym_crm.training.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.common.dto.TrainingAddRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainingTypeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingMapper trainingMapper;
    private final CrmMetrics crmMetrics;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainingResponseDto> getTraineeTrainings(
            String username, Date periodFrom, Date periodTo, String trainerName, String trainingType) {

        // Валідація існування через публічний фасад TraineeService
        traineeService.getProfile(username);

        Specification<Training> spec = Specification.where(TrainingSpecifications.hasTraineeUsername(username))
                .and(TrainingSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TrainingSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TrainingSpecifications.hasTrainerUsername(trainerName))
                .and(TrainingSpecifications.hasTrainingType(trainingType));

        List<Training> trainings = trainingRepository.findAll(spec);
        return trainingMapper.toTraineeReportResponseList(trainings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerTrainingResponseDto> getTrainerTrainings(
            String username, Date periodFrom, Date periodTo, String traineeName) {

        // Валідація існування через публічний фасад TrainerService
        trainerService.getProfile(username);

        Specification<Training> spec = Specification.where(TrainingSpecifications.hasTrainerUsername(username))
                .and(TrainingSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TrainingSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TrainingSpecifications.hasTraineeUsername(traineeName));

        List<Training> trainings = trainingRepository.findAll(spec);
        return trainingMapper.toTrainerReportResponseList(trainings);
    }

    @Override
    @Transactional
    public void addTraining(TrainingAddRequestDto request) {
        crmMetrics.getTrainingCreationTimer().record(() -> {
            Trainee trainee = traineeService.getEntityByUsername(request.getTraineeUsername());
            Trainer trainer = trainerService.getEntityByUsername(request.getTrainerUsername());

            if (!trainee.getUser().getIsActive()) {
                throw new InactiveUserException("Cannot add training: Trainee profile is inactive.");
            }
            if (!trainer.getUser().getIsActive()) {
                throw new InactiveUserException("Cannot add training: Trainer profile is inactive.");
            }

            Training training = Training.builder()
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingName(request.getTrainingName())
                    .trainingDate(request.getTrainingDate())
                    .trainingDuration(request.getTrainingDuration())
                    .trainingType(trainer.getSpecialization())
                    .build();

            trainingRepository.save(training);

            // Публікуємо подію: додавання зв'язку стажер-тренер відбудеться асинхронно через @ApplicationModuleListener
            eventPublisher.publishEvent(new TrainingCreatedEvent(
                    trainee.getUser().getUsername(),
                    trainer.getUser().getUsername(),
                    training.getTrainingName()
            ));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponseDto> getTrainingTypes() {
        return trainingMapper.toTypeResponseList(trainingTypeRepository.findAll());
    }
}