package com.authbase.api.dto;

import com.authbase.domain.AppSettings;

public record PublicSettingsResponse(
        boolean allowPublicRegistration
) {
    public static PublicSettingsResponse from(AppSettings settings) {
        return new PublicSettingsResponse(settings.allowPublicRegistration);
    }
}
