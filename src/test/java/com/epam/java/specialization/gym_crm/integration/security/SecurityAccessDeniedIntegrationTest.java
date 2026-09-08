package com.epam.java.specialization.gym_crm.integration.security;

import com.epam.java.specialization.gym_crm.integration.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class SecurityAccessDeniedIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainees/{username} - Fail (403 Forbidden when requesting foreign profile)")
    void getTraineeProfile_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        mockMvc.perform(get("/trainees/Trainee.Eleven"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /trainees/{username} - Fail (403 Forbidden when updating foreign profile)")
    void updateTraineeProfile_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        TraineeUpdateRequestDto updateRequest = TraineeUpdateRequestDto.builder()
                .username("Trainee.Eleven")
                .firstName("Hacker")
                .lastName("Eleven")
                .isActive(true)
                .build();

        mockMvc.perform(put("/trainees/Trainee.Eleven")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PUT /trainers/{username} - Fail (403 Forbidden when updating foreign profile)")
    void updateTrainerProfile_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        TrainerUpdateRequestDto updateRequest = TrainerUpdateRequestDto.builder()
                .username("Trainer.Eleven")
                .firstName("Hacker")
                .lastName("Eleven")
                .isActive(true)
                .build();

        mockMvc.perform(put("/trainers/Trainer.Eleven")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainings/trainee - Fail (403 Forbidden when requesting foreign trainings)")
    void getTraineeTrainings_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        mockMvc.perform(get("/trainings/trainee")
                        .param("username", "Trainee.Eleven"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}