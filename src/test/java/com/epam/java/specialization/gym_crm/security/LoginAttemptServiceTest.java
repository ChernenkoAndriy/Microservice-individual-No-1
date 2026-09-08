package com.epam.java.specialization.gym_crm.security;

import com.epam.java.specialization.gym_crm.exception.UserBlockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private LoginAttemptService loginAttemptService;

    private final String username = "john.doe";
    private final String ip = "192.168.1.1";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService(redisTemplate, 3, 5);
    }

    @Test
    @DisplayName("Should clear attempt and block keys on successful login")
    void loginSucceeded_ShouldDeleteRedisKeys() {
        loginAttemptService.loginSucceeded(username, ip);

        verify(redisTemplate, times(1)).delete("login:attempts:" + username + ":" + ip);
        verify(redisTemplate, times(1)).delete("login:block:" + username + ":" + ip);
    }

    @Test
    @DisplayName("Should increment attempts and set TTL on first failed login")
    void loginFailed_ShouldIncrementAndSetExpireOnFirstAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:attempts:" + username + ":" + ip)).thenReturn(1L);

        loginAttemptService.loginFailed(username, ip);

        verify(redisTemplate, times(1)).expire("login:attempts:" + username + ":" + ip, 5, TimeUnit.MINUTES);
        verify(valueOperations, never()).set(eq("login:block:" + username + ":" + ip), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should block user on third failed login attempt")
    void loginFailed_ShouldSetBlockKey_WhenMaxAttemptsReached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:attempts:" + username + ":" + ip)).thenReturn(3L);

        loginAttemptService.loginFailed(username, ip);

        verify(valueOperations, times(1)).set(eq("login:block:" + username + ":" + ip), anyLong(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should throw UserBlockedException when checking a blocked user")
    void checkIfBlocked_ShouldThrowUserBlockedException_WhenBlockedKeyExists() {
        when(redisTemplate.hasKey("login:block:" + username + ":" + ip)).thenReturn(true);

        assertThatThrownBy(() -> loginAttemptService.checkIfBlocked(username, ip))
                .isInstanceOf(UserBlockedException.class)
                .hasMessageContaining("blocked due to 3 unsuccessful login attempts");
    }

    @Test
    @DisplayName("Should not throw exception when user is not blocked")
    void checkIfBlocked_ShouldDoNothing_WhenUserNotBlocked() {
        when(redisTemplate.hasKey("login:block:" + username + ":" + ip)).thenReturn(false);

        loginAttemptService.checkIfBlocked(username, ip);

        verify(redisTemplate, times(1)).hasKey("login:block:" + username + ":" + ip);
    }

    @Test
    @DisplayName("Should return true for isBlocked when key exists")
    void isBlocked_ShouldReturnTrue_WhenKeyExists() {
        when(redisTemplate.hasKey("login:block:" + username + ":" + ip)).thenReturn(true);

        boolean blocked = loginAttemptService.isBlocked(username, ip);

        assertThat(blocked).isTrue();
    }
}