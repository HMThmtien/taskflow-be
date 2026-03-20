package com.taskflow.taskflow_be.module.profile.service;

import com.taskflow.taskflow_be.module.profile.dto.UserPreferenceDtos;
import com.taskflow.taskflow_be.module.profile.dto.UserProfileDtos;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserProfileService {
    UserProfileDtos.MeResponse getMyProfile();
    UserProfileDtos.MeResponse updateMyProfile(UserProfileDtos.UpdateProfileRequest request);
    UserProfileDtos.MeResponse uploadMyAvatar(MultipartFile file);
    UserProfileDtos.MeResponse removeMyAvatar();
    String resolveAvatarUrl(UUID userId);
    UserPreferenceDtos.PreferenceResponse getMyPreferences();
    UserPreferenceDtos.PreferenceResponse updateMyPreferences(UserPreferenceDtos.UpdatePreferenceRequest request);
}
