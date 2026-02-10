package com.jjt.platform.api.auth;

import com.jjt.platform.api.auth.dto.LoginRequest;
import com.jjt.platform.api.auth.dto.LoginResponse;
import com.jjt.platform.api.auth.dto.RegisterRequest;
import com.jjt.platform.config.security.CustomUserDetailsService;
import com.jjt.platform.config.security.JwtTokenProvider;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService userDetailsService,
                          UserJpaRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.username() == null || request.username().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (request.password() == null || request.password().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserEntity user = userDetailsService.getUserEntityByUsername(request.username());
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole(), user.getSponsorId());

            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole(), user.getSponsorId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user (admin only in production)")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        // Basic field validation
        if (request.username() == null || request.username().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (request.password() == null || request.password().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (request.role() == null || request.role().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // Validate role against supported roles (align with Role enum)
        if (!"JJT_ADMIN".equals(request.role()) && 
            !"ORG_ADMIN".equals(request.role()) && 
            !"SPONSOR".equals(request.role())) {
            return ResponseEntity.badRequest().body("Invalid role. Must be JJT_ADMIN, ORG_ADMIN, or SPONSOR");
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setEnabled(true);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<LoginResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring("Bearer ".length());
        String username = jwtTokenProvider.extractUsername(token);
        String role = jwtTokenProvider.extractRole(token);
        UUID sponsorId = jwtTokenProvider.extractSponsorId(token);

        return ResponseEntity.ok(new LoginResponse(token, username, role, sponsorId));
    }
}
