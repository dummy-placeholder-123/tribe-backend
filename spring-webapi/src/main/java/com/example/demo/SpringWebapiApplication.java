package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application.
 *
 * <p>This class is intentionally small: Boot's auto-configuration does the heavy lifting.
 * Starting the application spins up an embedded Tomcat server listening on port 8080 by default,
 * wires the dependency injection container, and discovers any REST controllers we declare.
 */
@SpringBootApplication
public class SpringWebapiApplication {

    /**
     * The canonical main method that delegates to Spring Boot.
     *
     * @param args command-line arguments propagated to the SpringApplication utility
     */
    public static void main(String[] args) {
        // SpringApplication.run bootstraps the application context and web server in one line.
        SpringApplication.run(SpringWebapiApplication.class, args);
    }
}
