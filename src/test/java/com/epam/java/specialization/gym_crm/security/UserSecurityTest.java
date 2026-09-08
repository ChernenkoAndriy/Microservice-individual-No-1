package com.epam.java.specialization.gym_crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityTest {

    private UserSecurity userSecurity;

    @BeforeEach
    void setUp() {
        userSecurity = new UserSecurity();
    }

    @Test
    @DisplayName("Should return true when authenticated username matches target username")
    void isOwner_ShouldReturnTrue_WhenUsernamesMatch() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("John.Doe");

        boolean result = userSecurity.isOwner(authentication, "John.Doe");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when authenticated username differs from target username")
    void isOwner_ShouldReturnFalse_WhenUsernamesDoNotMatch() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("John.Doe");

        boolean result = userSecurity.isOwner(authentication, "Jane.Doe");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when authentication or target username is null")
    void isOwner_ShouldReturnFalse_WhenParametersAreNull() {
        Authentication authentication = mock(Authentication.class);

        assertThat(userSecurity.isOwner(null, "John.Doe")).isFalse();
        assertThat(userSecurity.isOwner(authentication, null)).isFalse();
    }
}