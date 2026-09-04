package com.authbase.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSettingsRequest(
        @NotNull(message = "allowPublicRegistration es obligatorio")
        Boolean allowPublicRegistration
) {
}
