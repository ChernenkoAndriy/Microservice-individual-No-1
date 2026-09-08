package com.epam.java.specialization.gym_crm.training.internal;

import com.epam.java.specialization.gym_crm.common.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.training.Training;
import com.epam.java.specialization.gym_crm.training.TrainingType;
import com.epam.java.specialization.gym_crm.common.dto.TrainingTypeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
interface TrainingMapper {

    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDate", target = "trainingDate")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainingDuration", target = "trainingDuration")
    @Mapping(source = "training", target = "trainerName", qualifiedByName = "extractTrainerFullName")
    TraineeTrainingResponseDto toTraineeReportResponse(Training training);

    List<TraineeTrainingResponseDto> toTraineeReportResponseList(List<Training> trainings);

    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDate", target = "trainingDate")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainingDuration", target = "trainingDuration")
    @Mapping(source = "training", target = "traineeName", qualifiedByName = "extractTraineeFullName")
    TrainerTrainingResponseDto toTrainerReportResponse(Training training);

    List<TrainerTrainingResponseDto> toTrainerReportResponseList(List<Training> trainings);

    @Mapping(source = "trainingTypeName", target = "trainingType")
    @Mapping(source = "id", target = "trainingTypeId")
    TrainingTypeResponseDto toTypeResponse(TrainingType type);

    List<TrainingTypeResponseDto> toTypeResponseList(List<TrainingType> types);

    @Named("extractTrainerFullName")
    default String extractTrainerFullName(Training training) {
        if (training == null || training.getTrainer() == null || training.getTrainer().getUser() == null) {
            return "";
        }
        return training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName();
    }

    @Named("extractTraineeFullName")
    default String extractTraineeFullName(Training training) {
        if (training == null || training.getTrainee() == null || training.getTrainee().getUser() == null) {
            return "";
        }
        return training.getTrainee().getUser().getFirstName() + " " + training.getTrainee().getUser().getLastName();
    }
}