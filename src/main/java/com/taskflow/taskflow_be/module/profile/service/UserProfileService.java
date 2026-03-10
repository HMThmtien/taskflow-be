package com.taskflow.taskflow_be.module.profile.service;

import com.taskflow.taskflow_be.module.profile.dto.UserPreferenceDtos;
import com.taskflow.taskflow_be.module.profile.dto.UserProfileDtos;

public interface UserProfileService {
    UserProfileDtos.MeResponse getMyProfile();
    UserProfileDtos.MeResponse updateMyProfile(UserProfileDtos.UpdateProfileRequest request);
    UserPreferenceDtos.PreferenceResponse getMyPreferences();
    UserPreferenceDtos.PreferenceResponse updateMyPreferences(UserPreferenceDtos.UpdatePreferenceRequest request);
}