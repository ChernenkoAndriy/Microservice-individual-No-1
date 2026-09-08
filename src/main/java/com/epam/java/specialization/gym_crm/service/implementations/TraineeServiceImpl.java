package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TraineeMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
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
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return traineeMapper.toProfileResponse(trainee);
    }

    @Override
    @Transactional
    public TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        traineeMapper.updateEntityFromDto(request, trainee);
        traineeRepository.save(trainee);
        return traineeMapper.toUpdateResponse(trainee);
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        traineeRepository.delete(trainee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username) {
        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        List<Trainer> trainers = trainerRepository.findAvailableTrainersNotAssignedToTrainee(username);
        return traineeMapper.toTrainerShortResponseList(trainers);
    }

    @Override
    @Transactional
    public List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        if (request == null || request.isEmpty()) {
            trainee.getTrainers().clear();
        } else {
            List<String> usernames = request.stream()
                    .map(TrainerUsernameRequestDto::getUsername)
                    .collect(Collectors.toList());
            List<Trainer> newTrainers = trainerRepository.findByUserUsernameIn(usernames);
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
}