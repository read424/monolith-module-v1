package com.walrex.user.module_users.application.ports.input;

import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.UserDetailsResponseDTO;
import reactor.core.publisher.Mono;

public interface GetUserDetailsUseCase {
    Mono<UserDetailsResponseDTO> getUserDetails(String name_user);
}
