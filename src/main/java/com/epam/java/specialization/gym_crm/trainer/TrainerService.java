package com.epam.java.specialization.gym_crm.trainer;

import com.epam.java.specialization.gym_crm.common.dto.TrainerProfileResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerRegisterRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerShortResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUpdateRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUpdateResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.RegistrationResponseDto;

import java.util.List;

public interface TrainerService {
    RegistrationResponseDto register(TrainerRegisterRequestDto request);
    TrainerProfileResponseDto getProfile(String username);
    TrainerUpdateResponseDto updateProfile(String username, TrainerUpdateRequestDto request);
    void toggleActivation(String username, ActivationRequestDto request);
    boolean existsByUsername(String username);
    Trainer getEntityByUsername(String username);
    List<Trainer> findAllEntitiesByUsernames(List<String> usernames);
    List<TrainerShortResponseDto> findAvailableTrainersNotAssignedToTrainee(String traineeUsername);
}