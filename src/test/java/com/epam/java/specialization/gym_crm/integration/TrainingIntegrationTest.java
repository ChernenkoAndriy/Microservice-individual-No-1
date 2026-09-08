package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.dto.TrainingAddRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TrainingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainings/trainee - Success retrieving list with filters")
    void getTraineeTrainings_Success() throws Exception {
        mockMvc.perform(get("/trainings/trainee")
                        .param("username", "Trainee.Ten")
                        .param("trainingType", "Yoga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingName").value("Yoga Flow Intermediate"))
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"))
                .andExpect(jsonPath("$[0].trainerName").value("Trainer Ten"));
    }

    @Test
    @WithMockUser(username = "Ghost.Trainee", roles = "TRAINEE")
    @DisplayName("GET /trainings/trainee - Fail (EntityNotFoundException)")
    void getTraineeTrainings_Fail_NotFound() throws Exception {
        mockMvc.perform(get("/trainings/trainee")
                        .param("username", "Ghost.Trainee"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainee not found with username: Ghost.Trainee"));
    }

    

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("GET /trainings/trainer - Success retrieving list with filters")
    void getTrainerTrainings_Success() throws Exception {
        mockMvc.perform(get("/trainings/trainer")
                        .param("username", "Trainer.Ten")
                        .param("traineeName", "Trainee.Ten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingName").value("Yoga Flow Intermediate"))
                .andExpect(jsonPath("$[0].traineeName").value("Trainee Ten"));
    }

    @Test
    @WithMockUser(username = "Ghost.Trainer", roles = "TRAINER")
    @DisplayName("GET /trainings/trainer - Fail (EntityNotFoundException)")
    void getTrainerTrainings_Fail_NotFound() throws Exception {
        mockMvc.perform(get("/trainings/trainer")
                        .param("username", "Ghost.Trainer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainer not found with username: Ghost.Trainer"));
    }

    

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("POST /trainings - Success adding new training")
    void addTraining_Success() throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Trainee.Ten")
                .trainerUsername("Trainer.Eleven")
                .trainingName("Power Crossfit Session")
                .trainingDate(new Date())
                .trainingDuration(60)
                .build();

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("POST /trainings - Fail (InactiveUserException - Inactive Trainee)")
    void addTraining_Fail_InactiveTrainee() throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Trainee.Twelve")
                .trainerUsername("Trainer.Ten")
                .trainingName("Invalid Training")
                .trainingDate(new Date())
                .trainingDuration(45)
                .build();

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cannot add training: Trainee profile is inactive."));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("POST /trainings - Fail (InactiveUserException - Inactive Trainer)")
    void addTraining_Fail_InactiveTrainer() throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Trainee.Ten")
                .trainerUsername("Trainer.Twelve")
                .trainingName("Invalid Training")
                .trainingDate(new Date())
                .trainingDuration(45)
                .build();

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cannot add training: Trainer profile is inactive."));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("POST /trainings - Fail (EntityNotFoundException - Trainee doesn't exist)")
    void addTraining_Fail_TraineeNotFound() throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("Ghost.Trainee")
                .trainerUsername("Trainer.Ten")
                .trainingName("Ghost Training")
                .trainingDate(new Date())
                .trainingDuration(45)
                .build();

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainee not found with username: Ghost.Trainee"));
    }

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("POST /trainings - Fail (Validation Failed)")
    void addTraining_Fail_Validation() throws Exception {
        TrainingAddRequestDto invalidRequest = TrainingAddRequestDto.builder()
                .traineeUsername("")
                .trainerUsername("")
                .trainingName(" ")
                .trainingDate(null)
                .trainingDuration(null)
                .build();

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    

    @Test
    @WithMockUser(username = "Trainee.Ten", roles = "TRAINEE")
    @DisplayName("GET /trainings/types - Success retrieving list of types")
    void getTrainingTypes_Success() throws Exception {
        mockMvc.perform(get("/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"))
                .andExpect(jsonPath("$[1].trainingType").value("Crossfit"));
    }
}