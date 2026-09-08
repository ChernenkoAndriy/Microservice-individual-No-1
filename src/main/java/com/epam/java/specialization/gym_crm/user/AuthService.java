package com.epam.java.specialization.gym_crm.user;

import com.epam.java.specialization.gym_crm.common.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.JwtResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    JwtResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest);
    void changeLogin(ChangeLoginRequestDto request);
    void logout(HttpServletRequest request);
}