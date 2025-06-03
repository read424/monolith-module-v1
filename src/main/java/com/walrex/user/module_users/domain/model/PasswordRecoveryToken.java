package com.walrex.user.module_users.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordRecoveryToken {

    private String id;
    private Long userId;
    private String email;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int attempts;
    private TokenStatus status;

    /**
     * Estados posibles para un token de recuperación
     */
    public enum TokenStatus {
        PENDING,   // Pendiente de uso
        USED,      // Ya utilizado
        EXPIRED    // Expirado por tiempo
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) || status == TokenStatus.EXPIRED;
    }

    /**
     * Verifica si el token ya fue utilizado
     */
    public boolean isUsed() {
        return status == TokenStatus.USED;
    }

    /**
     * Verifica si el token está aún pendiente y válido
     */
    public boolean isValid() {
        return status == TokenStatus.PENDING && !isExpired();
    }

    /**
     * Verifica si un código proporcionado coincide con el almacenado
     */
    public boolean validateCode(String inputCode) {
        this.attempts++;
        return this.code.equals(inputCode);
    }

    /**
     * Marca el token como utilizado
     */
    public void markAsUsed() {
        this.status = TokenStatus.USED;
    }

    /**
     * Marca el token como expirado
     */
    public void markAsExpired() {
        this.status = TokenStatus.EXPIRED;
    }

    /**
     * Determina si se han excedido los intentos máximos
     */
    public boolean hasExceededMaxAttempts(int maxAttempts) {
        return this.attempts >= maxAttempts;
    }

}
