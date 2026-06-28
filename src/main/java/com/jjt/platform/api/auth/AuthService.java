package com.jjt.platform.api.auth;

import com.jjt.platform.api.auth.dto.ChangePasswordRequest;
import com.jjt.platform.api.auth.dto.CurrentUserResponse;
import com.jjt.platform.api.auth.dto.LoginRequest;
import com.jjt.platform.api.auth.dto.LoginResponse;
import com.jjt.platform.config.security.JwtProperties;
import com.jjt.platform.config.security.JwtTokenProvider;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.RefreshTokenEntity;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.RefreshTokenRepository;
import com.jjt.platform.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authManager,
                       JwtTokenProvider tokenProvider,
                       JwtProperties jwtProperties,
                       UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(principal);
        String rawRefreshToken = issueRefreshToken(principal);
        updateLastLogin(principal.getUsername());
        return LoginResponse.of(accessToken, rawRefreshToken, jwtProperties.getAccessTokenExpiryMs());
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (!stored.isValid()) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }
        stored.revoke();
        refreshTokenRepository.save(stored);

        JwtUserDetails principal = new JwtUserDetails(stored.getUser());
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRawRefreshToken = issueRefreshToken(principal);
        return LoginResponse.of(newAccessToken, newRawRefreshToken, jwtProperties.getAccessTokenExpiryMs());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    public CurrentUserResponse currentUser(JwtUserDetails principal) {
        return new CurrentUserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getRole().name(),
                principal.getSponsorId(),
                principal.getOrgId()
        );
    }

    @Transactional
    public void changePassword(JwtUserDetails principal, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private String issueRefreshToken(JwtUserDetails principal) {
        String raw = UUID.randomUUID().toString();
        String hash = sha256(raw);
        Instant expires = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiryMs());
        UserEntity userRef = userRepository.getReferenceById(principal.getId());
        refreshTokenRepository.save(new RefreshTokenEntity(userRef, hash, expires));
        return raw;
    }

    private void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
