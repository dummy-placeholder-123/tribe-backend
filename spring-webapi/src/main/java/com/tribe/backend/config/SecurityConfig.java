package com.tribe.backend.config;

import com.tribe.backend.security.JwtAuthenticationEntryPoint;
import com.tribe.backend.security.JwtAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

//    @Bean
//    @Order(1)
//    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
//        // Dedicated chain keeps actuator endpoints accessible without JWT interception.
//        return http
//            .securityMatcher(EndpointRequest.toAnyEndpoint())
//            .csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(registry -> registry.anyRequest().permitAll())
//            .build();
//    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Primary API chain retains JWT enforcement for all other requests.
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(registry -> registry
                .requestMatchers("/actuator/health", "/actuator/env", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
