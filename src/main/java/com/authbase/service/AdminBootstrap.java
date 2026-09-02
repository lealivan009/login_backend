package com.authbase.service;

import com.authbase.config.AuthProperties;
import com.authbase.domain.Role;
import com.authbase.domain.User;
import com.authbase.security.PasswordHasher;
import com.authbase.security.PasswordPolicy;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AdminBootstrap {

    private static final Logger LOG = Logger.getLogger(AdminBootstrap.class);

    private final AuthProperties properties;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;

    public AdminBootstrap(AuthProperties properties, PasswordHasher passwordHasher, PasswordPolicy passwordPolicy) {
        this.properties = properties;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        String email = properties.bootstrapAdminEmail();
        String password = properties.bootstrapAdminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (User.count() > 0) {
            return;
        }
        passwordPolicy.validate(password);
        User admin = new User();
        admin.email = User.normalizeEmail(email);
        admin.fullName = properties.bootstrapAdminName();
        admin.passwordHash = passwordHasher.hash(password);
        admin.role = Role.ADMIN;
        admin.persist();
        LOG.infof("Usuario administrador inicial creado: %s", admin.email);
    }
}
