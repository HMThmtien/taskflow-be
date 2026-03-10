package com.taskflow.taskflow_be.module.profile.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.profile.dto.UserPreferenceDtos;
import com.taskflow.taskflow_be.module.profile.dto.UserProfileDtos;
import com.taskflow.taskflow_be.module.profile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileDtos.MeResponse> me() {
        ensureAuthenticated();
        return ApiResponse.ok(userProfileService.getMyProfile());
    }

    @PatchMapping
    public ApiResponse<UserProfileDtos.MeResponse> update(
            @Valid @RequestBody UserProfileDtos.UpdateProfileRequest request
    ) {
        ensureAuthenticated();
        return ApiResponse.ok(userProfileService.updateMyProfile(request));
    }

    @GetMapping("/preferences")
    public ApiResponse<UserPreferenceDtos.PreferenceResponse> preferences() {
        ensureAuthenticated();
        return ApiResponse.ok(userProfileService.getMyPreferences());
    }

    @PatchMapping("/preferences")
    public ApiResponse<UserPreferenceDtos.PreferenceResponse> updatePreferences(
            @Valid @RequestBody UserPreferenceDtos.UpdatePreferenceRequest request
    ) {
        ensureAuthenticated();
        return ApiResponse.ok(userProfileService.updateMyPreferences(request));
    }

    private void ensureAuthenticated() {
        String username = SecurityUtils.currentUsername();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
    }
}