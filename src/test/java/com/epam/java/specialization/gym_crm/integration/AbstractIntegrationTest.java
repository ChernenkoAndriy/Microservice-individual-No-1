package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserDetailsService userDetailsService;

    protected static final PostgreSQLContainer<?> postgresContainer;
    protected static final GenericContainer<?> redisContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("gym_crm_test_db")
                .withUsername("gym_test_user")
                .withPassword("gym_test_password");
        postgresContainer.start();

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    protected String generateBearerToken(String username) {
        var userDetails = userDetailsService.loadUserByUsername(username);
        return "Bearer " + jwtService.generateToken(userDetails);
    }
}