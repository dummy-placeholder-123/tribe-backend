package com.tribe.backend.auth.service;

import com.tribe.backend.auth.domain.RefreshToken;
import com.tribe.backend.auth.dto.AuthResponse;
import com.tribe.backend.auth.dto.LoginRequest;
import com.tribe.backend.auth.dto.RefreshTokenRequest;
import com.tribe.backend.auth.repository.RefreshTokenRepository;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.config.JwtProperties;
import com.tribe.backend.security.JwtService;
import com.tribe.backend.security.UserPrincipal;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository, UserRepository userRepository,
                       JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(principal.getId()).orElseThrow());
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenTtlSeconds()));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, refreshTokenValue, jwtProperties.getAccessTokenTtlSeconds());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new NotFoundException("Refresh token expired");
        }
        UserAccount user = refreshToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        return new AuthResponse(accessToken, refreshToken.getToken(), jwtProperties.getAccessTokenTtlSeconds());
    }

    @Transactional
    public void logout(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }
}
