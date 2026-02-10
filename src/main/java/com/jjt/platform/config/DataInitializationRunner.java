package com.jjt.platform.config;

import com.jjt.platform.config.security.Role;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Profile({"dev", "local"})
public class DataInitializationRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationRunner.class);

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializationRunner(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        logger.info("Initializing default users for development/local environment");
        
        // Admin User
        if (!userRepository.existsByUsername("admin")) {
            UserEntity adminUser = new UserEntity();
            adminUser.setId(UUID.randomUUID());
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@jjt.org");
            adminUser.setRole(Role.JJT_ADMIN.name());
            adminUser.setEnabled(true);
            userRepository.save(adminUser);
            logger.info("✓ Default admin user created: username=admin");
        }

        // Sponsor User - need to link to seeded sponsor for sponsor flows to work
        if (!userRepository.existsByUsername("sponsor")) {
            UserEntity sponsorUser = new UserEntity();
            sponsorUser.setId(UUID.randomUUID());
            sponsorUser.setUsername("sponsor");
            sponsorUser.setPassword(passwordEncoder.encode("sponsor123"));
            sponsorUser.setEmail("sponsor@example.org");
            sponsorUser.setRole(Role.SPONSOR.name());
            sponsorUser.setEnabled(true);
            // Link to first seeded sponsor from V5 migration
            sponsorUser.setSponsorId(UUID.fromString("50000000-0000-0000-0000-000000000001"));
            userRepository.save(sponsorUser);
            logger.info("✓ Default sponsor user created: username=sponsor");
        }

        // Org Admin User
        if (!userRepository.existsByUsername("orgadmin")) {
            UserEntity orgAdminUser = new UserEntity();
            orgAdminUser.setId(UUID.randomUUID());
            orgAdminUser.setUsername("orgadmin");
            orgAdminUser.setPassword(passwordEncoder.encode("orgadmin123"));
            orgAdminUser.setEmail("orgadmin@jjt.org");
            orgAdminUser.setRole(Role.ORG_ADMIN.name());
            orgAdminUser.setEnabled(true);
            userRepository.save(orgAdminUser);
            logger.info("✓ Default org admin user created: username=orgadmin");
        }
        
        logger.info("Default users initialization complete");
    }
}
