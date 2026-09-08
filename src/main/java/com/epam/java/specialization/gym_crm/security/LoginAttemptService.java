package com.epam.java.specialization.gym_crm.security;

import com.epam.java.specialization.gym_crm.exception.UserBlockedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final String ATTEMPTS_PREFIX = "login:attempts:";
    private static final String BLOCK_PREFIX = "login:block:";

    private final int maxAttempts;
    private final long lockDurationMinutes;
    private final RedisTemplate<String, Object> redisTemplate;

    public LoginAttemptService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${application.security.brute-force.max-attempts:3}") int maxAttempts,
            @Value("${application.security.brute-force.lock-duration-minutes:5}") long lockDurationMinutes
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    public void loginSucceeded(String username, String clientIp) {
        String attemptKey = getAttemptKey(username, clientIp);
        String blockKey = getBlockKey(username, clientIp);
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(blockKey);
    }

    public void loginFailed(String username, String clientIp) {
        String attemptKey = getAttemptKey(username, clientIp);
        String blockKey = getBlockKey(username, clientIp);

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, lockDurationMinutes, TimeUnit.MINUTES);
        }

        if (attempts != null && attempts >= maxAttempts) {
            redisTemplate.opsForValue().set(blockKey, System.currentTimeMillis(), lockDurationMinutes, TimeUnit.MINUTES);
        }
    }

    public void checkIfBlocked(String username, String clientIp) {
        String blockKey = getBlockKey(username, clientIp);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new UserBlockedException(
                    String.format("User %s is blocked due to 3 unsuccessful login attempts from IP %s. Try again in %d minutes.",
                            username, clientIp, lockDurationMinutes)
            );
        }
    }

    public boolean isBlocked(String username, String clientIp) {
        String blockKey = getBlockKey(username, clientIp);
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    private String getAttemptKey(String username, String clientIp) {
        return ATTEMPTS_PREFIX + username + ":" + clientIp;
    }

    private String getBlockKey(String username, String clientIp) {
        return BLOCK_PREFIX + username + ":" + clientIp;
    }
}