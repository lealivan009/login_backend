package com.authbase.api.dto;

import com.authbase.domain.Role;
import com.authbase.domain.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String fullName,
        Role role,
        Instant createdAt,
        Instant lastLoginAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id,
                user.email,
                user.fullName,
                user.role,
                user.createdAt,
                user.lastLoginAt
        );
    }
}
