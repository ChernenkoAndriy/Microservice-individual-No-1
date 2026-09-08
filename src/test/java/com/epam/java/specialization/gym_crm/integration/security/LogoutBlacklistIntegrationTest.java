package com.epam.java.specialization.gym_crm.integration.security;

import com.epam.java.specialization.gym_crm.integration.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class LogoutBlacklistIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearRedis() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
        userRepository.findByUsername("Trainee.Ten").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(true);
            userRepository.saveAndFlush(user);
        });
    }


    @Test
    @DisplayName("POST /auth/logout - Should blacklist token in Redis and deny subsequent requests")
    void logout_ShouldBlacklistToken_AndDenySubsequentRequests() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("Trainee.Ten")
                .password("staticPass1")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("token").asText();
        String bearerHeader = "Bearer " + token;

        mockMvc.perform(get("/trainees/Trainee.Ten")
                        .header("Authorization", bearerHeader))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", bearerHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/trainees/Trainee.Ten")
                        .header("Authorization", bearerHeader))
                .andExpect(status().isForbidden());
    }
}