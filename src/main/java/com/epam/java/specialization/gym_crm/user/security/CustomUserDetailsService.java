package com.epam.java.specialization.gym_crm.user.security;

import com.epam.java.specialization.gym_crm.user.UserRoleProvider;
import com.epam.java.specialization.gym_crm.user.User;
import com.epam.java.specialization.gym_crm.user.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final List<UserRoleProvider> roleProviders;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String role = roleProviders.stream()
                .map(provider -> provider.resolveRole(username))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .findFirst()
                .orElse("USER");

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.getIsActive())
                .roles(role)
                .build();
    }
}