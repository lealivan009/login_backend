package com.authbase.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    public String id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    public String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    public Role role = Role.USER;

    @Column(name = "enabled", nullable = false)
    public boolean enabled = true;

    @Column(name = "failed_login_attempts", nullable = false)
    public int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    public Instant lockedUntil;

    @Column(name = "last_login_at")
    public Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        email = normalizeEmail(email);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        email = normalizeEmail(email);
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public static Optional<User> findByEmail(String email) {
        return find("email", normalizeEmail(email)).firstResultOptional();
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
