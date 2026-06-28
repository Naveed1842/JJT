package com.jjt.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class JjtPlatformApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Flyway migrations complete and the Spring context starts successfully
        // against a real PostgreSQL 16 instance provided by Testcontainers.
    }
}
