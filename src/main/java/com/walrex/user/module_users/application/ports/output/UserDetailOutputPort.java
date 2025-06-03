package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEmployee;
import reactor.core.publisher.Mono;

public interface UserDetailOutputPort {
    Mono<UserEmployee> findByUsername(String username);
}
