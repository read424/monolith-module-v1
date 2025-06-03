package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.domain.model.PasswordRecoveryData;
import reactor.core.publisher.Mono;

public interface PasswordRecoveryProducer {
    Mono<Void> sendPasswordRecoveryEvent(PasswordRecoveryData data);
}
