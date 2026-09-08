package com.epam.java.specialization.gym_crm.security;

import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String role = "USER";
        if (traineeRepository.findByUserUsername(username).isPresent()) {
            role = "TRAINEE";
        } else if (trainerRepository.findByUserUsername(username).isPresent()) {
            role = "TRAINER";
        }

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.getIsActive())
                .roles(role)
                .build();
    }
}