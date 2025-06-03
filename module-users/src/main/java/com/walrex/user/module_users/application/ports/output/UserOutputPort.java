package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.domain.model.UserDto;
import reactor.core.publisher.Mono;

public interface UserOutputPort extends GetUserOutputPort {
    Mono<UserDto> findByEmail(String email); // Para obtener datos del usuario
}
