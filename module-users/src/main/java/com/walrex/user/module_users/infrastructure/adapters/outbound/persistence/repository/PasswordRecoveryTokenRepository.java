package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.PasswordRecoveryTokenEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface PasswordRecoveryTokenRepository extends ReactiveCrudRepository<PasswordRecoveryTokenEntity, String> {
    Mono<PasswordRecoveryTokenEntity> findByCode(String code);

    Mono<PasswordRecoveryTokenEntity> findByUserIdAndStatus(Long userId, Integer status);

    // Para limpiar tokens antiguos si necesitas
    @Query("DELETE FROM seguridad.tb_password_recovery_tokens WHERE fecha_expiracion < :date")
    Mono<Void> deleteExpiredTokens(LocalDateTime date);
}
