package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {
    Mono<UserEntity> findByUsername(String username);

    Mono<UserEntity> findByEmail(String email);
}
