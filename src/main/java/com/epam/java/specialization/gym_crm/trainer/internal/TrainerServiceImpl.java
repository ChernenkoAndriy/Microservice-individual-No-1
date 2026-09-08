package com.epam.java.specialization.gym_crm.trainer.internal;

import com.epam.java.specialization.gym_crm.common.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.common.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.trainer.Trainer;
import com.epam.java.specialization.gym_crm.trainer.TrainerService;
import com.epam.java.specialization.gym_crm.common.dto.TrainerProfileResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerRegisterRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerShortResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUpdateRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUpdateResponseDto;
import com.epam.java.specialization.gym_crm.training.TrainingType;
import com.epam.java.specialization.gym_crm.training.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.user.UserService;
import com.epam.java.specialization.gym_crm.common.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.RegistrationResponseDto;
import com.epam.java.specialization.gym_crm.user.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService;
    private final TrainerMapper trainerMapper;
    private final CrmMetrics crmMetrics;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegistrationResponseDto register(TrainerRegisterRequestDto request) {
        TrainingType specialization = trainingTypeRepository.findById(request.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + request.getSpecializationId()));

        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(specialization);

        String rawPassword = userService.prepareUserCredentials(trainer.getUser());
        trainerRepository.save(trainer);
        crmMetrics.incrementTrainerRegistrations();

        UserDetails userDetails = userDetailsService.loadUserByUsername(trainer.getUser().getUsername());
        String token = jwtService.generateToken(userDetails);

        return new RegistrationResponseDto(trainer.getUser().getUsername(), rawPassword, token);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponseDto getProfile(String username) {
        Trainer trainer = getEntityByUsername(username);
        return trainerMapper.toProfileResponse(trainer);
    }

    @Override
    @Transactional
    public TrainerUpdateResponseDto updateProfile(String username, TrainerUpdateRequestDto request) {
        Trainer trainer = getEntityByUsername(username);
        trainerMapper.updateEntityFromDto(request, trainer);
        trainerRepository.save(trainer);
        return trainerMapper.toUpdateResponse(trainer);
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        userService.toggleActivation(username, request.getIsActive());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return trainerRepository.findByUserUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getEntityByUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAllEntitiesByUsernames(List<String> usernames) {
        return trainerRepository.findByUserUsernameIn(usernames);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponseDto> findAvailableTrainersNotAssignedToTrainee(String traineeUsername) {
        List<Trainer> trainers = trainerRepository.findAvailableTrainersNotAssignedToTrainee(traineeUsername);
        return trainerMapper.toTrainerShortResponseList(trainers);
    }
}