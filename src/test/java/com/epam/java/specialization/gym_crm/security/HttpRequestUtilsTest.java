package com.epam.java.specialization.gym_crm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRequestUtilsTest {

    @Test
    @DisplayName("Should extract first IP from X-Forwarded-For header when present")
    void getClientIP_ShouldExtractFromXForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18");

        String clientIp = HttpRequestUtils.getClientIP(request);

        assertThat(clientIp).isEqualTo("203.0.113.195");
    }

    @Test
    @DisplayName("Should fallback to getRemoteAddr when proxy headers are absent")
    void getClientIP_ShouldFallbackToRemoteAddr_WhenHeadersAreAbsent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String clientIp = HttpRequestUtils.getClientIP(request);

        assertThat(clientIp).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("Should return unknown when request object is null")
    void getClientIP_ShouldReturnUnknown_WhenRequestIsNull() {
        String clientIp = HttpRequestUtils.getClientIP(null);

        assertThat(clientIp).isEqualTo("unknown");
    }
}