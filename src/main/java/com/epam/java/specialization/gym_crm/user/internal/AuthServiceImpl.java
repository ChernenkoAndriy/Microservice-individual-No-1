package com.epam.java.specialization.gym_crm.user.internal;

import com.epam.java.specialization.gym_crm.common.exception.BadCredentialsException;
import com.epam.java.specialization.gym_crm.common.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.user.AuthService;
import com.epam.java.specialization.gym_crm.user.User;
import com.epam.java.specialization.gym_crm.common.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.common.dto.JwtResponseDto;
import com.epam.java.specialization.gym_crm.common.dto.LoginRequestDto;
import com.epam.java.specialization.gym_crm.user.security.HttpRequestUtils;
import com.epam.java.specialization.gym_crm.user.JwtService;
import com.epam.java.specialization.gym_crm.user.security.LoginAttemptService;
import com.epam.java.specialization.gym_crm.user.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public JwtResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String clientIp = HttpRequestUtils.getClientIP(httpRequest);
        loginAttemptService.checkIfBlocked(username, clientIp);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            loginAttemptService.loginSucceeded(username, clientIp);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return JwtResponseDto.builder()
                    .token(token)
                    .type("Bearer")
                    .build();
        } catch (org.springframework.security.core.AuthenticationException ex) {
            loginAttemptService.loginFailed(username, clientIp);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public void changeLogin(ChangeLoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + request.getUsername()));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password for user: " + request.getUsername());
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        SecurityContextHolder.clearContext();
    }
}