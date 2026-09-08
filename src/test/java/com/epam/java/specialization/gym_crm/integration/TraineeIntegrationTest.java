package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class TraineeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /trainees - Success registration (PermitAll)")
    void registerTrainee_Success() throws Exception {
        TraineeRegisterRequestDto request = TraineeRegisterRequestDto.builder()
                .firstName("Danylo")
                .lastName("Shlapak")
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();

        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Danylo.Shlapak"))
                .andExpect(jsonPath("$.password").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /trainees - Fail (Validation Failed)")
    void registerTrainee_Fail_Validation() throws Exception {
        TraineeRegisterRequestDto invalidRequest = TraineeRegisterRequestDto.builder()
                .firstName("")
                .lastName("  ")
                .build();

        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.firstName").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.lastName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainees/{username} - Success fetching profile")
    void getTraineeProfile_Success() throws Exception {
        mockMvc.perform(get("/trainees/Trainee.Ten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Trainee"))
                .andExpect(jsonPath("$.lastName").value("Ten"))
                .andExpect(jsonPath("$.address").value("Dnipro, Central St 4"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainees/{username} - Fail (EntityNotFoundException)")
    void getTraineeProfile_Fail_NotFound() throws Exception {
        mockMvc.perform(get("/trainees/Trainee.Ten"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /trainees/{username} - Success update profile")
    void updateTraineeProfile_Success() throws Exception {
        TraineeUpdateRequestDto updateRequest = TraineeUpdateRequestDto.builder()
                .username("Trainee.Ten")
                .firstName("UpdatedTrainee")
                .lastName("Ten")
                .isActive(true)
                .address("New Kyiv Address")
                .build();

        mockMvc.perform(put("/trainees/Trainee.Ten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedTrainee"))
                .andExpect(jsonPath("$.address").value("New Kyiv Address"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /trainees/{username} - Fail (Validation Failed)")
    void updateTraineeProfile_Fail_Validation() throws Exception {
        TraineeUpdateRequestDto invalidRequest = TraineeUpdateRequestDto.builder()
                .username("")
                .firstName("")
                .lastName("")
                .isActive(null)
                .build();

        mockMvc.perform(put("/trainees/Trainee.Ten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("DELETE /trainees/{username} - Success deletion")
    void deleteTraineeProfile_Success() throws Exception {
        mockMvc.perform(delete("/trainees/Trainee.Ten"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainees/{username}/unassigned-trainers - Success")
    void getUnassignedActiveTrainers_Success() throws Exception {
        mockMvc.perform(get("/trainees/Trainee.Ten/unassigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].username").value(org.hamcrest.Matchers.hasItem("Trainer.Thirteen")));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PUT /trainees/{username}/trainers - Success updating list")
    void updateTraineesTrainersList_Success() throws Exception {
        List<TrainerUsernameRequestDto> request = Collections.singletonList(
                TrainerUsernameRequestDto.builder().username("Trainer.Ten").build()
        );

        mockMvc.perform(put("/trainees/Trainee.Ten/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("Trainer.Ten"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PATCH /trainees/{username}/activation - Success deactivation")
    void toggleTraineeActivation_Success() throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(false)
                .build();

        mockMvc.perform(patch("/trainees/Trainee.Ten/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("PATCH /trainees/{username}/activation - Fail (IllegalStateException - Already same status)")
    void toggleTraineeActivation_Fail_AlreadySameStatus() throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(true)
                .build();

        mockMvc.perform(patch("/trainees/Trainee.Ten/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User profile active status is already true"));
    }
}