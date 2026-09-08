package com.epam.java.specialization.gym_crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponseDto {
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeShortResponseDto> trainees;
}