package com.walrex.user.module_users.domain.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class TotpService {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRecoveryCode() {
        // Generar código aleatorio de 6 dígitos
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
