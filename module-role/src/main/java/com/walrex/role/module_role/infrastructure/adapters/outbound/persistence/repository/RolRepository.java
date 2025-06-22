package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolEntity;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.projection.RolDetails;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RolRepository extends ReactiveCrudRepository<RolEntity, Long> {

    Mono<RolEntity> findByIdRol(Long id_rol);

    @Query("SELECT det_rol.id_rol, det_rol.idwin_state, win_sis.no_state, win_sis.type_state, win_sis.id_parent_win "+
            "FROM seguridad.tbroles_detalles AS det_rol "+
            "LEFT OUTER JOIN seguridad.win_sistemas AS win_sis ON win_sis.idwin_state=det_rol.idwin_state "+
            "WHERE det_rol.id_rol=:idrol")
    Flux<RolDetails> findDetailsByIdRol(Long idrol);
}
