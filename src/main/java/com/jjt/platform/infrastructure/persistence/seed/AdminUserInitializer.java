package com.jjt.platform.infrastructure.persistence.seed;

import com.jjt.platform.config.security.Role;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserInitializer(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${admin.default-email}") String adminEmail,
                                @Value("${admin.default-password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        var admin = new UserEntity(adminEmail, passwordEncoder.encode(adminPassword), Role.JJT_ADMIN, null, null);
        userRepository.save(admin);
        log.warn("Created default admin user: {}. Change the password immediately via PUT /api/auth/change-password", adminEmail);
    }
}
