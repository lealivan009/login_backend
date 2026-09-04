package com.authbase.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_settings")
public class AppSettings extends PanacheEntityBase {

    public static final String DEFAULT_ID = "default";

    @Id
    @Column(name = "id", length = 36, nullable = false)
    public String id = DEFAULT_ID;

    @Column(name = "allow_public_registration", nullable = false)
    public boolean allowPublicRegistration = true;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = DEFAULT_ID;
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public static AppSettings current() {
        AppSettings settings = findById(DEFAULT_ID);
        if (settings == null) {
            throw new IllegalStateException("App settings not initialized");
        }
        return settings;
    }
}
