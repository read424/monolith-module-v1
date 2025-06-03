package com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ModulesUrlRepository extends R2dbcRepository<ModulesUrl, Long> {

    Flux<ModulesUrl> findAll();

    @Query("SELECT * FROM gateway.tb_modules WHERE module_name=:moduleName AND status='1'")
    Mono<ModulesUrl> findByModulesUrlName(String moduleName);

    @Query("SELECT * FROM gateway.tb_modules WHERE path=:namePath AND status='1'")
    Mono<ModulesUrl> findByPath(String namePath);

    @Query("SELECT * FROM gateway.tb_modules WHERE is_pattern=true AND status='1'")
    Flux<ModulesUrl> findAllPatterns();
}
