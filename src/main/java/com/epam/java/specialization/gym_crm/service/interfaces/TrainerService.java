package com.epam.java.specialization.gym_crm.service.interfaces;
import com.epam.java.specialization.gym_crm.dto.*;

public interface TrainerService {

    
    RegistrationResponseDto register(TrainerRegisterRequestDto request);

    
    TrainerProfileResponseDto getProfile(String username);

    
    TrainerUpdateResponseDto updateProfile(String username, TrainerUpdateRequestDto request);

    
    void toggleActivation(String username, ActivationRequestDto request);
}