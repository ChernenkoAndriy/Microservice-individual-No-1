package com.epam.java.specialization.gym_crm.integration.security;

import com.epam.java.specialization.gym_crm.integration.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BruteForceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/login - Should block user (423 Locked) on 4th login attempt after 3 failures")
    void login_ShouldBlockUser_AfterThreeFailedAttempts() throws Exception {
        LoginRequestDto wrongRequest = LoginRequestDto.builder()
                .username("Trainee.Ten")
                .password("wrong_password")
                .build();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized());
        }

        LoginRequestDto correctRequest = LoginRequestDto.builder()
                .username("Trainee.Ten")
                .password("staticPass1")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("Locked"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("is blocked due to 3 unsuccessful login attempts")));
    }
}