package com.epam.java.specialization.gym_crm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTypeResponseDto {
    private String trainingType;
    private Long trainingTypeId;
}