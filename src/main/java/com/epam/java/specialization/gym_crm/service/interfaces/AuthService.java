package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.dto.JwtResponseDto;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    JwtResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest);
    void changeLogin(ChangeLoginRequestDto request);
    void logout(HttpServletRequest request);
}