package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.domain.model.PasswordRecoveryToken;
import reactor.core.publisher.Mono;

public interface PasswordRecoveryRepository {
    Mono<PasswordRecoveryToken> saveRecoveryCode(PasswordRecoveryToken token);
    Mono<PasswordRecoveryToken> findByCode(String code);
    Mono<PasswordRecoveryToken> findByUserIdAndStatusPending(Long userId);
    Mono<Void> updateToken(PasswordRecoveryToken token);
    Mono<Void> cleanupExpiredTokens();
}
