package com.walrex.user.module_users.application.ports.input;

import reactor.core.publisher.Mono;

public interface RecoveryPasswordUseCase {
    Mono<Void> initiatePasswordRecovery(String email);
}
