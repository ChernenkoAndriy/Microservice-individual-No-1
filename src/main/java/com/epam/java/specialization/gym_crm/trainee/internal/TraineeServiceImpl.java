package com.epam.java.specialization.gym_crm.trainee.internal;

import com.epam.java.specialization.gym_crm.common.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.common.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.trainee.Trainee;
import com.epam.java.specialization.gym_crm.trainee.TraineeService;
import com.epam.java.specialization.gym_crm.common.dto.TraineeProfileResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeRegisterRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeUpdateRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.TraineeUpdateResponseDto;
import com.epam.java.specialization.gym_crm.trainer.Trainer;
import com.epam.java.specialization.gym_crm.trainer.TrainerService;
import com.epam.java.specialization.gym_crm.common.dto.TrainerShortResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.TrainerUsernameRequestDto;
import com.epam.java.specialization.gym_crm.user.UserService;
import com.epam.java.specialization.gym_crm.common.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.RegistrationResponseDto;
import com.epam.java.specialization.gym_crm.user.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerService trainerService;
    private final UserService userService;
    private final TraineeMapper traineeMapper;
    private final CrmMetrics crmMetrics;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegistrationResponseDto register(TraineeRegisterRequestDto request) {
        Trainee trainee = traineeMapper.toEntity(request);
        String rawPassword = userService.prepareUserCredentials(trainee.getUser());
        traineeRepository.save(trainee);
        crmMetrics.incrementTraineeRegistrations();

        UserDetails userDetails = userDetailsService.loadUserByUsername(trainee.getUser().getUsername());
        String token = jwtService.generateToken(userDetails);

        return new RegistrationResponseDto(trainee.getUser().getUsername(), rawPassword, token);
    }

    @Override
    @Transactional(readOnly = true)
    public TraineeProfileResponseDto getProfile(String username) {
        Trainee trainee = getEntityByUsername(username);
        return traineeMapper.toProfileResponse(trainee);
    }

    @Override
    @Transactional
    public TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request) {
        Trainee trainee = getEntityByUsername(username);
        traineeMapper.updateEntityFromDto(request, trainee);
        traineeRepository.save(trainee);
        return traineeMapper.toUpdateResponse(trainee);
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        Trainee trainee = getEntityByUsername(username);
        traineeRepository.delete(trainee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username) {
        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return trainerService.findAvailableTrainersNotAssignedToTrainee(username);
    }

    @Override
    @Transactional
    public List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request) {
        Trainee trainee = getEntityByUsername(username);

        if (request == null || request.isEmpty()) {
            trainee.getTrainers().clear();
        } else {
            List<String> usernames = request.stream()
                    .map(TrainerUsernameRequestDto::getUsername)
                    .collect(Collectors.toList());
            List<Trainer> newTrainers = trainerService.findAllEntitiesByUsernames(usernames);
            trainee.setTrainers(newTrainers);
        }

        Trainee savedTrainee = traineeRepository.save(trainee);
        return traineeMapper.toTrainerShortResponseList(savedTrainee.getTrainers());
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        userService.toggleActivation(username, request.getIsActive());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return traineeRepository.findByUserUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee getEntityByUsername(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
    }

    @Override
    @Transactional
    public void linkTrainerToTrainee(String traineeUsername, String trainerUsername) {
        Trainee trainee = getEntityByUsername(traineeUsername);
        Trainer trainer = trainerService.getEntityByUsername(trainerUsername);

        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new ArrayList<>());
        }
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
        }
    }
}