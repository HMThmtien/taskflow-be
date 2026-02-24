package com.taskflow.taskflow_be.common.util;

import com.taskflow.taskflow_be.security.user.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (UserPrincipal) auth.getPrincipal();
        return principal.getId();
    }
}