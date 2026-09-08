package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TrainerMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private CrmMetrics crmMetrics;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    @DisplayName("Should register trainer and map specialized field requirements correctly with JWT Token")
    void register_ShouldRegisterTrainerSuccessfully_WhenTrainingTypeExists() {
        TrainerRegisterRequestDto request = TrainerRegisterRequestDto.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .specializationId(5L)
                .build();

        TrainingType trainingType = TrainingType.builder().id(5L).trainingTypeName("Boxing").build();
        User user = User.builder().firstName("Mike").lastName("Tyson").username("Mike.Tyson").password("secret").build();
        Trainer trainer = Trainer.builder().user(user).build();
        UserDetails mockUserDetails = mock(UserDetails.class);

        when(trainingTypeRepository.findById(5L)).thenReturn(Optional.of(trainingType));
        when(trainerMapper.toEntity(request)).thenReturn(trainer);
        when(userService.prepareUserCredentials(user)).thenReturn("secret");
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(userDetailsService.loadUserByUsername("Mike.Tyson")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("mockJwtToken");

        RegistrationResponseDto response = trainerService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("Mike.Tyson");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(trainer.getSpecialization()).isEqualTo(trainingType);

        verify(userService, times(1)).prepareUserCredentials(user);
        verify(trainerRepository, times(1)).save(trainer);
        verify(jwtService, times(1)).generateToken(mockUserDetails);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException during registration if specialization ID is invalid")
    void register_ShouldThrowException_WhenSpecializationIdIsInvalid() {
        TrainerRegisterRequestDto request = TrainerRegisterRequestDto.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .specializationId(99L)
                .build();

        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.register(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TrainingType not found with ID: 99");

        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    @DisplayName("Should fetch trainer profiles flawlessly when matching entity exists")
    void getProfile_ShouldReturnProfileResponse() {
        String username = "Mike.Tyson";
        Trainer trainer = Trainer.builder()
                .user(User.builder().username(username).build())
                .build();
        TrainerProfileResponseDto profileDto = TrainerProfileResponseDto.builder()
                .firstName("Mike")
                .specialization("Boxing")
                .build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileDto);

        TrainerProfileResponseDto actualProfile = trainerService.getProfile(username);

        assertThat(actualProfile).isNotNull();
        assertThat(actualProfile.getSpecialization()).isEqualTo("Boxing");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when profile request refers to an absent trainer")
    void getProfile_ShouldThrowEntityNotFound_WhenTrainerDoesNotExist() {
        String username = "Invisible.Man";
        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getProfile(username))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found with username: Invisible.Man");
    }

    @Test
    @DisplayName("Should update trainer user details flawlessly")
    void updateProfile_ShouldPersistTrainerFormAndOutputMappedDto() {
        String username = "Mike.Tyson";
        TrainerUpdateRequestDto updateReq = TrainerUpdateRequestDto.builder()
                .username(username)
                .firstName("IronMike")
                .build();

        Trainer trainer = Trainer.builder().user(User.builder().username(username).build()).build();
        TrainerUpdateResponseDto responseDto = TrainerUpdateResponseDto.builder().username(username).firstName("IronMike").build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));
        doNothing().when(trainerMapper).updateEntityFromDto(updateReq, trainer);
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(trainerMapper.toUpdateResponse(trainer)).thenReturn(responseDto);

        TrainerUpdateResponseDto actualUpdate = trainerService.updateProfile(username, updateReq);

        assertThat(actualUpdate.getFirstName()).isEqualTo("IronMike");
        verify(trainerRepository, times(1)).save(trainer);
    }
}