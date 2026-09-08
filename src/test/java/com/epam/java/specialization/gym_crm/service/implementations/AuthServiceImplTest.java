package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.exception.BadCredentialsException;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should successfully change user password when old password matches")
    void changeLogin_ShouldUpdatePassword_WhenCredentialsAreValid() {
        User user = User.builder()
                .username("John.Doe")
                .password("encodedOldPassword")
                .isActive(true)
                .build();

        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("John.Doe")
                .oldPassword("oldSecretPassword")
                .newPassword("newSuperSecretPassword")
                .build();

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldSecretPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSuperSecretPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.changeLogin(request);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(passwordEncoder, times(1)).matches("oldSecretPassword", "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode("newSuperSecretPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user does not exist during password change")
    void changeLogin_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Missing.User")
                .oldPassword("somePass")
                .newPassword("newPass")
                .build();

        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changeLogin(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: Missing.User");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when old password is incorrect")
    void changeLogin_ShouldThrowBadCredentialsException_WhenOldPasswordIsIncorrect() {
        User user = User.builder()
                .username("John.Doe")
                .password("encodedActualPassword")
                .build();

        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("John.Doe")
                .oldPassword("wrongOldPassword")
                .newPassword("brandNewPassword")
                .build();

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encodedActualPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changeLogin(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid old password for user: John.Doe");

        verify(userRepository, never()).save(any(User.class));
    }
}