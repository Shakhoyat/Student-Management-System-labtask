package com.example.webapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    /**
     * Check if the current user is accessing their own profile
     */
    public boolean isOwnProfile(Long profileId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            Long userProfileId = userDetails.getProfileId();
            return userProfileId != null && userProfileId.equals(profileId);
        }
        return false;
    }

    /**
     * Check if user has teacher role
     */
    public boolean isTeacher(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
    }

    /**
     * Check if user has student role
     */
    public boolean isStudent(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }
}
