package com.tribe.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Minimal smoke test verifying the application context spins up successfully.
 *
 * <p>This is an excellent guardrail: if future configuration changes break auto-configuration,
 * this test fails fast.
 */
@SpringBootTest
@ActiveProfiles("test")
class SpringWebapiApplicationTests {

    @Test
    void contextLoads() {
        // The mere presence of this method triggers a full Spring Boot startup for verification.
    }
}
