package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence;

import com.walrex.user.module_users.application.ports.output.UserOutputPort;
import com.walrex.user.module_users.domain.model.UserDto;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginRequestDto;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.mapper.UserEntityMapper;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersPersistenceAdapter implements UserOutputPort {
    private final UserRepository userRepository;
    private final UserEntityMapper userMapper;


    @Override
    public Mono<UserEntity> getUserValidLogin(LoginRequestDto request) {
        return userRepository.findByUsername(request.getUsername());
    }

    @Override
    public Mono<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .doOnNext(user -> log.info("Usuario encontrado: {}", user.getEmail()))
                .map(userMapper::entityToDto);
    }

}
