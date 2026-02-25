package com.taskflow.taskflow_be.module.auth.controller;

import com.taskflow.taskflow_be.module.auth.service.UserAdminService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService service;

    @PatchMapping("/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(@PathVariable String username,
                           @RequestParam @NotBlank String role) {
        service.changeRole(username, role);
    }
}