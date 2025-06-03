package com.walrex.user.module_users.application.ports.input;

import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginRequestDto;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginResponseDTO;
import reactor.core.publisher.Mono;

public interface SignInUserCase {
    Mono<LoginResponseDTO> validUserPassword(LoginRequestDto request);
}
