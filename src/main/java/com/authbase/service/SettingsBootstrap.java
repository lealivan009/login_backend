package com.authbase.service;

import com.authbase.domain.AppSettings;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SettingsBootstrap {

    private static final Logger LOG = Logger.getLogger(SettingsBootstrap.class);

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (AppSettings.findById(AppSettings.DEFAULT_ID) != null) {
            return;
        }
        AppSettings settings = new AppSettings();
        settings.id = AppSettings.DEFAULT_ID;
        settings.allowPublicRegistration = true;
        settings.persist();
        LOG.info("Configuración general inicial creada (registro público habilitado)");
    }
}
