package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence;

import com.walrex.user.module_users.application.ports.output.PasswordRecoveryRepository;
import com.walrex.user.module_users.domain.model.PasswordRecoveryToken;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.mapper.PasswordRecoveryTokenMapper;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository.PasswordRecoveryTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryPersistenceAdapter implements PasswordRecoveryRepository {
    private final PasswordRecoveryTokenMapper tokenMapper;
    private final PasswordRecoveryTokenRepository tokenRepository;

    @Override
    public Mono<PasswordRecoveryToken> saveRecoveryCode(PasswordRecoveryToken token) {
        log.debug("Guardando token de recuperación para usuario: {}", token.getUserId());
        return Mono.just(token)
                .map(tokenMapper::domainToEntity)
                .flatMap(tokenRepository::save)
                .doOnSuccess(saved-> log.info("✅ Token guardado con éxito: id={}", saved.getId()) )
                .map(tokenMapper::entityToDomain);
    }

    @Override
    public Mono<PasswordRecoveryToken> findByCode(String code) {
        return null;
    }

    @Override
    public Mono<PasswordRecoveryToken> findByUserIdAndStatusPending(Long userId) {
        return null;
    }

    @Override
    public Mono<Void> updateToken(PasswordRecoveryToken token) {
        return null;
    }

    @Override
    public Mono<Void> cleanupExpiredTokens() {
        return null;
    }
}
