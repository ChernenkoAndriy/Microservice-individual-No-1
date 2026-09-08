package com.epam.java.specialization.gym_crm.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    public boolean isOwner(Authentication authentication, String username) {
        if (authentication == null || username == null) {
            return false;
        }
        return authentication.getName().equals(username);
    }
}