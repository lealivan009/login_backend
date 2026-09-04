package com.authbase.api.dto;

import com.authbase.domain.AppSettings;

import java.time.Instant;

public record SettingsResponse(
        boolean allowPublicRegistration,
        Instant updatedAt
) {
    public static SettingsResponse from(AppSettings settings) {
        return new SettingsResponse(settings.allowPublicRegistration, settings.updatedAt);
    }
}
