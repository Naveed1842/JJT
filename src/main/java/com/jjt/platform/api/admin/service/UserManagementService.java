package com.jjt.platform.api.admin.service;

import com.jjt.platform.api.admin.dto.CreateOrgAdminUserRequest;
import com.jjt.platform.api.admin.dto.CreateSponsorUserRequest;
import com.jjt.platform.api.admin.dto.UserResponse;
import com.jjt.platform.config.security.Role;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final SponsorJpaRepository sponsorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository,
                                 SponsorJpaRepository sponsorRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sponsorRepository = sponsorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createSponsorUser(CreateSponsorUserRequest request) {
        if (!sponsorRepository.existsById(request.sponsorId())) {
            throw new DomainException("Sponsor not found: " + request.sponsorId());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException("A user with email '" + request.email() + "' already exists");
        }
        var user = new UserEntity(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.SPONSOR,
                request.sponsorId(),
                null
        );
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse createOrgAdminUser(CreateOrgAdminUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException("A user with email '" + request.email() + "' already exists");
        }
        var user = new UserEntity(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.ORG_ADMIN,
                null,
                request.orgId()
        );
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse setActive(UUID userId, boolean active) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found: " + userId));
        user.setActive(active);
        return UserResponse.from(userRepository.save(user));
    }
}
