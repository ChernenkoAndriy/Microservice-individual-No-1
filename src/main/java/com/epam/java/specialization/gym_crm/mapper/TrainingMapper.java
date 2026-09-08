package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    
    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDate", target = "trainingDate")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainingDuration", target = "trainingDuration")
    @Mapping(target = "trainerName", expression = "java(training.getTrainer().getUser().getFirstName() + \" \" + training.getTrainer().getUser().getLastName())")
    TraineeTrainingResponseDto toTraineeReportResponse(Training training);

    List<TraineeTrainingResponseDto> toTraineeReportResponseList(List<Training> trainings);

    
    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDate", target = "trainingDate")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainingDuration", target = "trainingDuration")
    @Mapping(target = "traineeName", expression = "java(training.getTrainee().getUser().getFirstName() + \" \" + training.getTrainee().getUser().getLastName())")
    TrainerTrainingResponseDto toTrainerReportResponse(Training training);

    List<TrainerTrainingResponseDto> toTrainerReportResponseList(List<Training> trainings);

    
    @Mapping(source = "trainingTypeName", target = "trainingType")
    @Mapping(source = "id", target = "trainingTypeId")
    TrainingTypeResponseDto toTypeResponse(TrainingType type);

    List<TrainingTypeResponseDto> toTypeResponseList(List<TrainingType> types);
}