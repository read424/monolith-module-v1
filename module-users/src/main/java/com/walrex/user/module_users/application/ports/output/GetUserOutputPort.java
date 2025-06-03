package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginRequestDto;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import reactor.core.publisher.Mono;

public interface GetUserOutputPort {
    Mono<UserEntity> getUserValidLogin(LoginRequestDto request);
}
