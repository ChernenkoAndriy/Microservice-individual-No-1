package com.epam.java.specialization.gym_crm.trainee;

import com.epam.java.specialization.gym_crm.common.dto.TraineeProfileResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeRegisterRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeUpdateRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeUpdateResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerShortResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUsernameRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.RegistrationResponseDto;

import java.util.List;

public interface TraineeService {
    RegistrationResponseDto register(TraineeRegisterRequestDto request);
    TraineeProfileResponseDto getProfile(String username);
    TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request);
    void deleteProfile(String username);
    List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username);
    List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request);
    void toggleActivation(String username, ActivationRequestDto request);
    boolean existsByUsername(String username);
    Trainee getEntityByUsername(String username);
    void linkTrainerToTrainee(String traineeUsername, String trainerUsername);
}