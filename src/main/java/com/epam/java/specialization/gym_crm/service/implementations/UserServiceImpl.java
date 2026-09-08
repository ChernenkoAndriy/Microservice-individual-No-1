package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public String prepareUserCredentials(User user) {
        String baseUsername = user.getFirstName() + "." + user.getLastName();
        String finalUsername = generateUniqueUsername(baseUsername);
        String rawPassword = generateRandomPassword();

        user.setUsername(finalUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));

        return rawPassword;
    }

    @Override
    @Transactional
    public void toggleActivation(String username, boolean isActive) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        if (user.getIsActive().equals(isActive)) {
            throw new IllegalStateException("User profile active status is already " + isActive);
        }
        user.setIsActive(isActive);
        userRepository.save(user);
    }

    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}