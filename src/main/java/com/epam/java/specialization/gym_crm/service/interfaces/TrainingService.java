package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.*;
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