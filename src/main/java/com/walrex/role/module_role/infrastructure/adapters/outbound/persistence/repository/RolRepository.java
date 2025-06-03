package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RolRepository extends ReactiveCrudRepository<RolEntity, Long> {

    Mono<RolEntity> findByIdRol(Long id_rol);
}
