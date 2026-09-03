package com.authbase.service;

import com.authbase.api.dto.CreateUserRequest;
import com.authbase.api.dto.UpdateUserRequest;
import com.authbase.api.dto.UserResponse;
import com.authbase.domain.RefreshToken;
import com.authbase.domain.Role;
import com.authbase.domain.User;
import com.authbase.helpers.UserProfiles;
import com.authbase.error.ApiException;
import com.authbase.security.PasswordHasher;
import com.authbase.security.PasswordPolicy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UserService {

    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;

    public UserService(PasswordHasher passwordHasher, PasswordPolicy passwordPolicy) {
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
    }

    public List<UserResponse> list() {
        return User.<User>find("ORDER BY createdAt DESC").list().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        passwordPolicy.validate(request.password());
        String email = User.normalizeEmail(request.email());
        if (User.findByEmail(email).isPresent()) {
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "Ya existe una cuenta con ese email");
        }
        User user = new User();
        user.email = email;
        user.firstName = request.firstName().trim();
        user.lastName = request.lastName().trim();
        user.passwordHash = passwordHasher.hash(request.password());
        user.role = request.role() == null ? Role.USER : request.role();
        UserProfiles.set(
                user,
                request.documentNumber(),
                request.phone(),
                request.birthDate(),
                request.street(),
                request.city(),
                request.province(),
                request.postalCode()
        );
        user.persist();
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(String id, String actorId, UpdateUserRequest request) {
        User user = requireUser(id);
        boolean self = user.id.equals(actorId);

        if (request.firstName() != null) {
            user.firstName = request.firstName().trim();
        }
        if (request.lastName() != null) {
            user.lastName = request.lastName().trim();
        }
        UserProfiles.patch(
                user,
                request.documentNumber(),
                request.phone(),
                request.birthDate(),
                request.street(),
                request.city(),
                request.province(),
                request.postalCode()
        );
        if (request.role() != null && request.role() != user.role) {
            if (self) {
                throw ApiException.badRequest("CANNOT_CHANGE_OWN_ROLE", "No podés cambiar tu propio rol");
            }
            if (user.role == Role.ADMIN && request.role() != Role.ADMIN && adminCount() <= 1) {
                throw ApiException.badRequest("LAST_ADMIN", "Tiene que quedar al menos un administrador");
            }
            user.role = request.role();
            RefreshToken.revokeAllForUser(user);
        }
        if (request.enabled() != null && request.enabled() != user.enabled) {
            if (self) {
                throw ApiException.badRequest("CANNOT_DISABLE_SELF", "No podés deshabilitar tu propia cuenta");
            }
            user.enabled = request.enabled();
            if (user.enabled) {
                user.failedLoginAttempts = 0;
                user.lockedUntil = null;
            } else {
                RefreshToken.revokeAllForUser(user);
            }
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(String id, String actorId) {
        User user = requireUser(id);
        if (user.id.equals(actorId)) {
            throw ApiException.badRequest("CANNOT_DELETE_SELF", "No podés borrar tu propia cuenta");
        }
        if (user.role == Role.ADMIN && adminCount() <= 1) {
            throw ApiException.badRequest("LAST_ADMIN", "Tiene que quedar al menos un administrador");
        }
        RefreshToken.delete("user", user);
        user.delete();
    }

    private User requireUser(String id) {
        User user = User.findById(id);
        if (user == null) {
            throw ApiException.notFound("USER_NOT_FOUND", "No se encontró el usuario");
        }
        return user;
    }

    private long adminCount() {
        return User.count("role", Role.ADMIN);
    }
}
