package com.epam.java.specialization.gym_crm.service.implementations;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should prepare unique username without suffix if it does not exist yet")
    void prepareUserCredentials_ShouldGenerateNonConflictingUsernameAndPassword() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByUsername("John.Doe")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));

        String rawPassword = userService.prepareUserCredentials(user);

        assertThat(user.getUsername()).isEqualTo("John.Doe");
        assertThat(rawPassword).isNotBlank().hasSize(10);
        assertThat(user.getPassword()).isEqualTo("encoded_" + rawPassword);

        verify(userRepository, times(1)).existsByUsername("John.Doe");
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("Should prepare unique username with numbered suffix when base username exists")
    void prepareUserCredentials_ShouldAppendIncrementalSuffix_WhenUsernameConflictsExist() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByUsername("John.Doe")).thenReturn(true);
        when(userRepository.existsByUsername("John.Doe1")).thenReturn(true);
        when(userRepository.existsByUsername("John.Doe2")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));

        String rawPassword = userService.prepareUserCredentials(user);

        assertThat(user.getUsername()).isEqualTo("John.Doe2");
        assertThat(rawPassword).isNotBlank().hasSize(10);
        assertThat(user.getPassword()).isEqualTo("encoded_" + rawPassword);

        verify(userRepository, times(1)).existsByUsername("John.Doe");
        verify(userRepository, times(1)).existsByUsername("John.Doe1");
        verify(userRepository, times(1)).existsByUsername("John.Doe2");
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("Should successfully toggle user active status from active to inactive")
    void toggleActivation_ShouldUpdateStatusSuccessfully() {
        String username = "John.Doe";
        User user = User.builder().username(username).isActive(true).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.toggleActivation(username, false);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw IllegalStateException if toggled status matches current status")
    void toggleActivation_ShouldThrowIllegalStateException_WhenStatusIsAlreadyTheSame() {
        String username = "John.Doe";
        User user = User.builder().username(username).isActive(true).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.toggleActivation(username, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User profile active status is already true");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if toggling activation on a missing user profile")
    void toggleActivation_ShouldThrowEntityNotFound_WhenUserDoesNotExist() {
        String username = "Missing.User";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActivation(username, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: Missing.User");
    }
}