package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.CreateOrgAdminUserRequest;
import com.jjt.platform.api.admin.dto.CreateSponsorUserRequest;
import com.jjt.platform.api.admin.dto.UserResponse;
import com.jjt.platform.api.admin.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('JJT_ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping("/sponsor")
    public ResponseEntity<UserResponse> createSponsorUser(@Valid @RequestBody CreateSponsorUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createSponsorUser(request));
    }

    @PostMapping("/org")
    public ResponseEntity<UserResponse> createOrgAdminUser(@Valid @RequestBody CreateOrgAdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createOrgAdminUser(request));
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PutMapping("/{userId}/activate")
    public UserResponse activateUser(@PathVariable("userId") UUID userId) {
        return userManagementService.setActive(userId, true);
    }

    @PutMapping("/{userId}/deactivate")
    public UserResponse deactivateUser(@PathVariable("userId") UUID userId) {
        return userManagementService.setActive(userId, false);
    }
}
