package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainingAddRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainingTypeResponseDto;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.exception.InactiveUserException;
import com.epam.java.specialization.gym_crm.mapper.TrainingMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.repository.specification.TraineeSpecifications;
import com.epam.java.specialization.gym_crm.repository.specification.TrainerSpecifications;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;
    private final CrmMetrics crmMetrics;

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainingResponseDto> getTraineeTrainings(
            String username, Date periodFrom, Date periodTo, String trainerName, String trainingType) {

        
        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }

        
        Specification<Training> spec = Specification.where(TraineeSpecifications.hasTraineeUsername(username))
                .and(TraineeSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TraineeSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TraineeSpecifications.hasTrainerUsername(trainerName))
                .and(TraineeSpecifications.hasTrainingType(trainingType));

        List<Training> trainings = trainingRepository.findAll(spec);
        return trainingMapper.toTraineeReportResponseList(trainings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerTrainingResponseDto> getTrainerTrainings(
            String username, Date periodFrom, Date periodTo, String traineeName) {

        
        if (!trainerRepository.findByUserUsername(username).isPresent()) {
            throw new EntityNotFoundException("Trainer not found with username: " + username);
        }

        Specification<Training> spec = Specification.where(TrainerSpecifications.hasTrainerUsername(username))
                .and(TrainerSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TrainerSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TrainerSpecifications.hasTraineeUsername(traineeName));

        List<Training> trainings = trainingRepository.findAll(spec);
        return trainingMapper.toTrainerReportResponseList(trainings);
    }

    @Override
    @Transactional
    public void addTraining(TrainingAddRequestDto request) {
        crmMetrics.getTrainingCreationTimer().record(() -> {
        Trainee trainee = traineeRepository.findByUserUsername(request.getTraineeUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + request.getTraineeUsername()));

        Trainer trainer = trainerRepository.findByUserUsername(request.getTrainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + request.getTrainerUsername()));

        if (!trainee.getUser().getIsActive()) {
            throw new InactiveUserException("Cannot add training: Trainee profile is inactive.");
        }
        if (!trainer.getUser().getIsActive()) {
            throw new InactiveUserException("Cannot add training: Trainer profile is inactive.");
        }

        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new ArrayList<>());
        }
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
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
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponseDto> getTrainingTypes() {
        return trainingMapper.toTypeResponseList(trainingTypeRepository.findAll());
    }
}