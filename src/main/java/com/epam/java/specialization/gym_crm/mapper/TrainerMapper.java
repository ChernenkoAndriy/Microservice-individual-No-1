package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))public interface TrainerMapper {

    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "specialization", ignore = true) 
    @Mapping(source = "firstName", target = "user.firstName")
    @Mapping(source = "lastName", target = "user.lastName")
    @Mapping(target = "user.isActive", constant = "true") 
    @Mapping(target = "user.id", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    Trainer toEntity(TrainerRegisterRequestDto dto);

    
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerProfileResponseDto toProfileResponse(Trainer entity);

    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "specialization", ignore = true) 
    @Mapping(source = "firstName", target = "user.firstName")
    @Mapping(source = "lastName", target = "user.lastName")
    @Mapping(source = "isActive", target = "user.isActive")
    @Mapping(target = "user.id", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    void updateEntityFromDto(TrainerUpdateRequestDto dto, @MappingTarget Trainer entity);

    
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerUpdateResponseDto toUpdateResponse(Trainer entity);

    
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TraineeShortResponseDto toTraineeShortResponse(Trainee trainee);

    
    List<TraineeShortResponseDto> toTraineeShortResponseList(List<Trainee> trainees);
}