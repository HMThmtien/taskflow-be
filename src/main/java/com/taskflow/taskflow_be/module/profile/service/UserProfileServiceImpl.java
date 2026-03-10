package com.taskflow.taskflow_be.module.profile.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.profile.dto.UserPreferenceDtos;
import com.taskflow.taskflow_be.module.profile.dto.UserProfileDtos;
import com.taskflow.taskflow_be.module.profile.entity.UserPreferenceEntity;
import com.taskflow.taskflow_be.module.profile.repository.UserPreferenceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public UserProfileServiceImpl(
            UserRepository userRepository,
            UserPreferenceRepository preferenceRepository
    ) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public UserProfileDtos.MeResponse getMyProfile() {
        var user = getCurrentUser();
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileDtos.MeResponse updateMyProfile(UserProfileDtos.UpdateProfileRequest request) {
        var user = getCurrentUser();

        if (request.getEmail() != null) {
            var nextEmail = request.getEmail().trim();
            var currentEmail = user.getEmail();

            if (currentEmail == null || !currentEmail.equalsIgnoreCase(nextEmail)) {
                if (userRepository.existsByEmailIgnoreCase(nextEmail)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(nextEmail);
            }
        } else {
            user.setEmail(null);
        }

        user.setFullName(trimToNull(request.getFullName()));
        user.setJobTitle(trimToNull(request.getJobTitle()));
        user.setBio(trimToNull(request.getBio()));
        user.setTimezone(trimToNull(request.getTimezone()));
        user.setLocale(trimToNull(request.getLocale()));

        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    public UserPreferenceDtos.PreferenceResponse getMyPreferences() {
        var preference = getOrCreatePreference();
        return toPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public UserPreferenceDtos.PreferenceResponse updateMyPreferences(
            UserPreferenceDtos.UpdatePreferenceRequest request
    ) {
        var preference = getOrCreatePreference();

        preference.setTheme(request.getTheme().trim());
        preference.setDensity(request.getDensity().trim());
        preference.setDefaultStartPage(request.getDefaultStartPage().trim());
        preference.setEmailNotifications(request.isEmailNotifications());
        preference.setInAppNotifications(request.isInAppNotifications());

        preferenceRepository.save(preference);
        return toPreferenceResponse(preference);
    }

    private UserEntity getCurrentUser() {
        var username = SecurityUtils.currentUsername();

        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new IllegalStateException("Unauthenticated");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private UserPreferenceEntity getOrCreatePreference() {
        var user = getCurrentUser();

        return preferenceRepository.findById(user.getId())
                .orElseGet(() -> {
                    var entity = UserPreferenceEntity.builder()
                            .user(user)
                            .theme("system")
                            .density("comfortable")
                            .defaultStartPage("dashboard")
                            .emailNotifications(true)
                            .inAppNotifications(true)
                            .build();

                    return preferenceRepository.save(entity);
                });
    }

    private UserProfileDtos.MeResponse toProfileResponse(UserEntity user) {
        var dto = new UserProfileDtos.MeResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setJobTitle(user.getJobTitle());
        dto.setTimezone(user.getTimezone());
        dto.setLocale(user.getLocale());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }

    private UserPreferenceDtos.PreferenceResponse toPreferenceResponse(UserPreferenceEntity entity) {
        var dto = new UserPreferenceDtos.PreferenceResponse();
        dto.setTheme(entity.getTheme());
        dto.setDensity(entity.getDensity());
        dto.setDefaultStartPage(entity.getDefaultStartPage());
        dto.setEmailNotifications(entity.isEmailNotifications());
        dto.setInAppNotifications(entity.isInAppNotifications());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}