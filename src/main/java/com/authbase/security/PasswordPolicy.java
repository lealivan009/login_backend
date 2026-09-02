package com.authbase.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.regex.Pattern;

@ApplicationScoped
public class PasswordPolicy {

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");

    public void validate(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw com.authbase.error.ApiException.badRequest(
                    "WEAK_PASSWORD",
                    "La contraseña debe tener entre 8 y 72 caracteres"
            );
        }
        if (!UPPER.matcher(password).matches()
                || !LOWER.matcher(password).matches()
                || !DIGIT.matcher(password).matches()) {
            throw com.authbase.error.ApiException.badRequest(
                    "WEAK_PASSWORD",
                    "La contraseña debe incluir mayúscula, minúscula y un número"
            );
        }
    }
}
