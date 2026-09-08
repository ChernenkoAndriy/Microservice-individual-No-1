package com.epam.java.specialization.gym_crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUsernameRequestDto {
    @NotBlank(message = "Trainer username is required")
    private String username;
}