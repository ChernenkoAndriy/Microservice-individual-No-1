package com.epam.java.specialization.gym_crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTrainingResponseDto {
    private String trainingName;
    private Date trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String trainerName;
}