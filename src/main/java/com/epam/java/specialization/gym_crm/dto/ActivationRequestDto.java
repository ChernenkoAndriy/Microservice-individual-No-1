package com.epam.java.specialization.gym_crm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationRequestDto {
    @NotNull(message = "Is Active status is required")
    private Boolean isActive;
}