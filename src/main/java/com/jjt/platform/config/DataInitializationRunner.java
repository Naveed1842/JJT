package com.jjt.platform.config;

import com.jjt.platform.config.security.Role;
import com.jjt.platform.infrastructure.persistence.entity.UserEntity;
import com.jjt.platform.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class DataInitializationRunner implements CommandLineRunner {

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
            System.out.println("✓ Admin user created: username=admin, password=admin123");
        }

        // Sponsor User
        if (!userRepository.existsByUsername("sponsor")) {
            UserEntity sponsorUser = new UserEntity();
            sponsorUser.setId(UUID.randomUUID());
            sponsorUser.setUsername("sponsor");
            sponsorUser.setPassword(passwordEncoder.encode("sponsor123"));
            sponsorUser.setEmail("sponsor@example.org");
            sponsorUser.setRole(Role.SPONSOR.name());
            sponsorUser.setEnabled(true);
            userRepository.save(sponsorUser);
            System.out.println("✓ Sponsor user created: username=sponsor, password=sponsor123");
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
            System.out.println("✓ Org Admin user created: username=orgadmin, password=orgadmin123");
        }
    }
}
