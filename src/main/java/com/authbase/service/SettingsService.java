package com.authbase.service;

import com.authbase.api.dto.SettingsResponse;
import com.authbase.api.dto.UpdateSettingsRequest;
import com.authbase.domain.AppSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SettingsService {

    public AppSettings current() {
        return AppSettings.current();
    }

    public SettingsResponse get() {
        return SettingsResponse.from(current());
    }

    @Transactional
    public SettingsResponse update(UpdateSettingsRequest request) {
        AppSettings settings = current();
        settings.allowPublicRegistration = request.allowPublicRegistration();
        return SettingsResponse.from(settings);
    }
}
