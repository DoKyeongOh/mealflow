package com.odk.pjt.mealflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the persisted {@link com.odk.pjt.mealflow.user.User#getId()} for the current OAuth2 session principal.
     */
    public static Long requireCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MealflowOidcUser principal)) {
            throw new IllegalStateException("Expected authenticated MealflowOidcUser");
        }
        return principal.getUserId();
    }
}
