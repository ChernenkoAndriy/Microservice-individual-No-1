package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TraineeMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private CrmMetrics crmMetrics;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    @DisplayName("Should register new trainee, prepare credentials and return registration details with JWT token")
    void register_ShouldSaveTraineeAndReturnCredentials() {
        TraineeRegisterRequestDto request = TraineeRegisterRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        User user = User.builder().firstName("John").lastName("Doe").username("John.Doe").password("generatedPass").build();
        Trainee trainee = Trainee.builder().user(user).build();
        UserDetails mockUserDetails = mock(UserDetails.class);

        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(userService.prepareUserCredentials(user)).thenReturn("generatedPass");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);
        when(userDetailsService.loadUserByUsername("John.Doe")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("mockJwtToken");

        RegistrationResponseDto response = traineeService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("John.Doe");
        assertThat(response.getPassword()).isEqualTo("generatedPass");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");

        verify(userService, times(1)).prepareUserCredentials(user);
        verify(traineeRepository, times(1)).save(trainee);
        verify(jwtService, times(1)).generateToken(mockUserDetails);
    }

    @Test
    @DisplayName("Should retrieve trainee profile by username successfully")
    void getProfile_ShouldReturnTraineeProfileResponseDto() {
        String username = "Alice.Wonder";
        Trainee trainee = Trainee.builder()
                .user(User.builder().username(username).build())
                .build();
        TraineeProfileResponseDto expectedProfile = TraineeProfileResponseDto.builder()
                .firstName("Alice")
                .isActive(true)
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(expectedProfile);

        TraineeProfileResponseDto actualProfile = traineeService.getProfile(username);

        assertThat(actualProfile).isNotNull();
        assertThat(actualProfile.getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when profile request uses non-existing username")
    void getProfile_ShouldThrowEntityNotFoundException_WhenTraineeDoesNotExist() {
        String username = "Ghost.Rider";
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getProfile(username))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found with username: Ghost.Rider");
    }

    @Test
    @DisplayName("Should update existing trainee profile from Update DTO successfully")
    void updateProfile_ShouldUpdateTraineeDataAndReturnDto() {
        String username = "Alice.Wonder";
        TraineeUpdateRequestDto updateRequest = TraineeUpdateRequestDto.builder()
                .username(username)
                .firstName("NewAlice")
                .build();

        Trainee trainee = Trainee.builder().user(User.builder().username(username).build()).build();
        TraineeUpdateResponseDto responseDto = TraineeUpdateResponseDto.builder().username(username).firstName("NewAlice").build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        doNothing().when(traineeMapper).updateEntityFromDto(updateRequest, trainee);
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(traineeMapper.toUpdateResponse(trainee)).thenReturn(responseDto);

        TraineeUpdateResponseDto result = traineeService.updateProfile(username, updateRequest);

        assertThat(result.getFirstName()).isEqualTo("NewAlice");
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    @DisplayName("Should successfully delete trainee and clear relationship records with trainers")
    void deleteProfile_ShouldClearRelationshipsAndPerformDeletion() {
        String username = "John.Doe";
        List<Trainer> trainersList = new ArrayList<>();
        trainersList.add(Trainer.builder().build());
        Trainee trainee = Trainee.builder()
                .user(User.builder().username(username).build())
                .trainers(trainersList)
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        doNothing().when(traineeRepository).delete(trainee);

        traineeService.deleteProfile(username);

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    @DisplayName("Should successfully retrieve unassigned active trainers")
    void getUnassignedActiveTrainers_ShouldReturnTrainersList() {
        String username = "John.Doe";
        List<Trainer> mockAvailableTrainers = Collections.singletonList(Trainer.builder().build());
        List<TrainerShortResponseDto> mockResponse = Collections.singletonList(TrainerShortResponseDto.builder().username("Trainer.Joe").build());

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(Trainee.builder().build()));
        when(trainerRepository.findAvailableTrainersNotAssignedToTrainee(username)).thenReturn(mockAvailableTrainers);
        when(traineeMapper.toTrainerShortResponseList(mockAvailableTrainers)).thenReturn(mockResponse);

        List<TrainerShortResponseDto> results = traineeService.getUnassignedActiveTrainers(username);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("Trainer.Joe");
    }

    @Test
    @DisplayName("Should update trainers list of a trainee based on DTOs")
    void updateTrainersList_ShouldSaveNewTrainersRelationAndReturnShortResponse() {
        String username = "John.Doe";
        Trainee trainee = Trainee.builder()
                .user(User.builder().username(username).build())
                .trainers(new ArrayList<>())
                .build();

        TrainerUsernameRequestDto reqDto = TrainerUsernameRequestDto.builder().username("Trainer.Max").build();
        List<TrainerUsernameRequestDto> request = Collections.singletonList(reqDto);

        Trainer trainer = Trainer.builder().user(User.builder().username("Trainer.Max").build()).build();
        List<Trainer> mockTrainers = Collections.singletonList(trainer);
        TrainerShortResponseDto responseDto = TrainerShortResponseDto.builder().username("Trainer.Max").build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsernameIn(Collections.singletonList("Trainer.Max"))).thenReturn(mockTrainers);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);
        when(traineeMapper.toTrainerShortResponseList(any())).thenReturn(Collections.singletonList(responseDto));

        List<TrainerShortResponseDto> results = traineeService.updateTrainersList(username, request);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getUsername()).isEqualTo("Trainer.Max");
        verify(traineeRepository, times(1)).save(trainee);
    }
}