package com.taskflow.taskflow_be.module.profile.service;

import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.profile.dto.UserPreferenceDtos;
import com.taskflow.taskflow_be.module.profile.dto.UserProfileDtos;
import com.taskflow.taskflow_be.module.profile.entity.UserPreferenceEntity;
import com.taskflow.taskflow_be.module.profile.repository.UserPreferenceRepository;
import com.taskflow.taskflow_be.storage.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final int MAX_FILE_NAME_LENGTH = 160;

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final StorageProperties storageProperties;
    private final FileStorageService fileStorageService;

    public UserProfileServiceImpl(
            UserRepository userRepository,
            UserPreferenceRepository preferenceRepository,
            StorageProperties storageProperties,
            FileStorageService fileStorageService
    ) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.storageProperties = storageProperties;
        this.fileStorageService = fileStorageService;
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
    @Transactional
    public UserProfileDtos.MeResponse uploadMyAvatar(MultipartFile file) {
        var user = getCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Avatar file must not be empty");
        }

        validateAvatar(file);

        if (user.getAvatarStorageKey() != null && !user.getAvatarStorageKey().isBlank()) {
            fileStorageService.delete(
                    user.getAvatarStorageProvider(),
                    user.getAvatarStorageBucket(),
                    user.getAvatarStorageKey(),
                    user.getAvatarUrl()
            );
        }

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String safeName = UUID.randomUUID() + "_" + originalName;
        String objectKey = Paths.get("avatars", user.getId().toString(), safeName).toString().replace('\\', '/');
        FileStorageService.StoredFile storedFile = fileStorageService.store(objectKey, file);

        user.setAvatarUrl(storedFile.legacyPath());
        user.setAvatarStorageProvider(storedFile.provider());
        user.setAvatarStorageBucket(storedFile.bucket());
        user.setAvatarStorageKey(storedFile.key());

        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileDtos.MeResponse removeMyAvatar() {
        var user = getCurrentUser();

        if (user.getAvatarStorageKey() != null && !user.getAvatarStorageKey().isBlank()) {
            fileStorageService.delete(
                    user.getAvatarStorageProvider(),
                    user.getAvatarStorageBucket(),
                    user.getAvatarStorageKey(),
                    user.getAvatarUrl()
            );
        }

        user.setAvatarUrl(null);
        user.setAvatarStorageProvider(null);
        user.setAvatarStorageBucket(null);
        user.setAvatarStorageKey(null);

        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    public String resolveAvatarUrl(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        String resolved = resolveAvatarAccessUrl(user);
        if (resolved == null || resolved.isBlank()) {
            throw new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Avatar not found");
        }

        return resolved;
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
        dto.setAvatarUrl(resolveAvatarUrl(user));
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

    private String resolveAvatarUrl(UserEntity user) {
        if (user.getAvatarStorageKey() != null && !user.getAvatarStorageKey().isBlank()) {
            return "/api/users/me/public/" + user.getId() + "/avatar";
        }
        return trimToNull(user.getAvatarUrl());
    }

    private String resolveAvatarAccessUrl(UserEntity user) {
        if (user.getAvatarStorageKey() != null && !user.getAvatarStorageKey().isBlank()) {
            return fileStorageService.createAccessUrl(
                    user.getAvatarStorageProvider(),
                    user.getAvatarStorageBucket(),
                    user.getAvatarStorageKey(),
                    user.getAvatarUrl()
            );
        }

        return trimToNull(user.getAvatarUrl());
    }

    private void validateAvatar(MultipartFile file) {
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Avatar file is too large");
        }

        String contentType = file.getContentType();
        Set<String> allowedContentTypes = new HashSet<>(storageProperties.getAllowedContentTypes());
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Avatar file type is not allowed");
        }

        if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Avatar must be an image");
        }
    }

    private String sanitizeOriginalName(String originalFilename) {
        String baseName = originalFilename == null
                ? "avatar"
                : Paths.get(originalFilename).getFileName().toString();
        String sanitized = baseName
                .replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("[^a-zA-Z0-9._ -]", "_")
                .trim();

        if (sanitized.isBlank()) {
            sanitized = "avatar";
        }

        if (sanitized.length() > MAX_FILE_NAME_LENGTH) {
            int extensionIndex = sanitized.lastIndexOf('.');
            if (extensionIndex > 0 && extensionIndex < sanitized.length() - 1) {
                String extension = sanitized.substring(extensionIndex);
                int baseLength = Math.max(1, MAX_FILE_NAME_LENGTH - extension.length());
                sanitized = sanitized.substring(0, baseLength) + extension;
            } else {
                sanitized = sanitized.substring(0, MAX_FILE_NAME_LENGTH);
            }
        }

        return sanitized;
    }
}
