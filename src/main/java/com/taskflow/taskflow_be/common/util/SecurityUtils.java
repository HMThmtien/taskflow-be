package com.taskflow.taskflow_be.common.util;

import com.taskflow.taskflow_be.security.user.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils(){}

    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;

        if (auth.getPrincipal() instanceof UserPrincipal up) {
            return up.getId();
        }
        return null;
    }

    public static String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getUsername();
        if (principal instanceof String s) return s; // đôi khi principal = "anonymousUser"
        return null;
    }
}