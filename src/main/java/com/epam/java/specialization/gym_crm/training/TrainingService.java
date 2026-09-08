package com.epam.java.specialization.gym_crm.training;

import com.epam.java.specialization.gym_crm.common.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainingAddRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainingTypeResponseDto;

import java.util.Date;
import java.util.List;

public interface TrainingService {

    
    List<TraineeTrainingResponseDto> getTraineeTrainings(
            String username, Date periodFrom, Date periodTo, String trainerName, String trainingType);

    
    List<TrainerTrainingResponseDto> getTrainerTrainings(
            String username, Date periodFrom, Date periodTo, String traineeName);

    
    void addTraining(TrainingAddRequestDto request);

    
    List<TrainingTypeResponseDto> getTrainingTypes();
}