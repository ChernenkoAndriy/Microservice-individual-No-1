package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.*;
import java.util.List;

public interface TraineeService {

    RegistrationResponseDto register(TraineeRegisterRequestDto request);

    TraineeProfileResponseDto getProfile(String username);

    TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request);

    void deleteProfile(String username);

    List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username);

    List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request);

    void toggleActivation(String username, ActivationRequestDto request);
}