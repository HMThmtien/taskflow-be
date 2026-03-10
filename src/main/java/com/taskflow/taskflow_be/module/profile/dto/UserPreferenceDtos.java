package com.taskflow.taskflow_be.module.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class UserPreferenceDtos {

    @Getter
    @Setter
    public static class PreferenceResponse {
        private String theme;
        private String density;
        private String defaultStartPage;
        private boolean emailNotifications;
        private boolean inAppNotifications;
    }

    @Getter
    @Setter
    public static class UpdatePreferenceRequest {
        @NotBlank
        @Size(max = 20)
        private String theme;

        @NotBlank
        @Size(max = 20)
        private String density;

        @NotBlank
        @Size(max = 50)
        private String defaultStartPage;

        private boolean emailNotifications;
        private boolean inAppNotifications;
    }
}