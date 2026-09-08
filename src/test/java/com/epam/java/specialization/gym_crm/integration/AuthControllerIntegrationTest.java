package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.findByUsername("Trainer.Ten").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("staticPass1"));
            userRepository.save(user);
        });
        userRepository.findByUsername("Trainee.Ten").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("staticPass1"));
            userRepository.save(user);
        });
    }

    @Test
    @DisplayName("POST /auth/login - Success login with valid credentials (active user)")
    void login_Success() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("Trainee.Ten")
                .password("staticPass1")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - Fail: invalid credentials (Bad Credentials)")
    void login_Fail_WrongPassword() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("Trainee.Ten")
                .password("wrong_password")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Fail: user is blocked or inactive")
    void login_Fail_InactiveUser() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("Trainee.Twelve")
                .password("staticPass1")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Fail: user does not exist")
    void login_Fail_UserNotFound() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("Non.Existent")
                .password("staticPass1")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PUT /auth/password - Success password change")
    void changeLogin_Success() throws Exception {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Trainer.Ten")
                .oldPassword("staticPass1")
                .newPassword("newPassword20")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("Trainer.Ten")
                .password("newPassword20")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        ChangeLoginRequestDto revertRequest = ChangeLoginRequestDto.builder()
                .username("Trainer.Ten")
                .oldPassword("newPassword20")
                .newPassword("staticPass1")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revertRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainee.Eleven", roles = "TRAINEE")
    @DisplayName("PUT /auth/password - Fail: old password is incorrect")
    void changeLogin_Fail_WrongOldPassword() throws Exception {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Trainee.Eleven")
                .oldPassword("incorrect_old_pass")
                .newPassword("brandNewPass")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid old password for user: Trainee.Eleven"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /auth/password - Fail: attempt to change password for non-existent user (EntityNotFoundException)")
    void changeLogin_Fail_UserNotFound() throws Exception {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Ghost.User")
                .oldPassword("staticPass1")
                .newPassword("newPassword10")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with username: Ghost.User"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /auth/password - Fail: DTO validation failed (empty payload)")
    void changeLogin_Fail_ValidationFailed() throws Exception {
        ChangeLoginRequestDto invalidRequest = ChangeLoginRequestDto.builder()
                .username("")
                .oldPassword(" ")
                .newPassword("")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}