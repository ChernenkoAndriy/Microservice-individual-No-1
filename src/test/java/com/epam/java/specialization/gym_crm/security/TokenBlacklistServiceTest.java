package com.epam.java.specialization.gym_crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private TokenBlacklistService tokenBlacklistService;

    private final long jwtExpirationMs = 3600000L;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate, jwtExpirationMs);
    }

    @Test
    @DisplayName("Should successfully put token to Redis blacklist with TTL")
    void blacklistToken_ShouldSetKeyInRedis() {
        String token = "valid.jwt.token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.blacklistToken(token);

        verify(valueOperations, times(1)).set("token:blacklist:" + token, true, jwtExpirationMs, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("Should do nothing when blacklisting null or blank token")
    void blacklistToken_ShouldDoNothing_WhenTokenIsBlankOrNull() {
        tokenBlacklistService.blacklistToken(null);
        tokenBlacklistService.blacklistToken("   ");

        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("Should return true when token exists in Redis blacklist")
    void isBlacklisted_ShouldReturnTrue_WhenKeyExists() {
        String token = "blacklisted.token";
        when(redisTemplate.hasKey("token:blacklist:" + token)).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).hasKey("token:blacklist:" + token);
    }

    @Test
    @DisplayName("Should return false when token is absent in Redis blacklist")
    void isBlacklisted_ShouldReturnFalse_WhenKeyDoesNotExist() {
        String token = "clean.token";
        when(redisTemplate.hasKey("token:blacklist:" + token)).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when checking null or blank token")
    void isBlacklisted_ShouldReturnFalse_WhenTokenIsBlankOrNull() {
        assertThat(tokenBlacklistService.isBlacklisted(null)).isFalse();
        assertThat(tokenBlacklistService.isBlacklisted("")).isFalse();

        verifyNoInteractions(redisTemplate);
    }
}