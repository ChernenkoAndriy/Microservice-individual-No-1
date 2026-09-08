package com.epam.java.specialization.gym_crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDto {
    private String username;
    private String password;
    private String token;
    @Builder.Default
    private String type = "Bearer";

    public RegistrationResponseDto(String username, String password) {
        this.username = username;
        this.password = password;
        this.type = "Bearer";
    }

    public RegistrationResponseDto(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.type = "Bearer";
    }
}