package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.dto.WindowSystemDTO;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.RolesDetailsEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserRolDetailsRepository extends ReactiveCrudRepository<RolesDetailsEntity, Long> {

    @Query("SELECT det_rol.id_rol, det_rol.idwin_sate, win_sis.no_state, win_sis.type_state, win_sis.id_parent_win "+
            "FROM seguridad.tbroles_detalle AS det_rol "+
            "LEFT OUTER JOIN seguridad.win_sistemas AS win_sis ON win_sis.id_winstate=det_rol.idwin_state "+
            "WHERE det_rol.id_rol=:idrol")
    Flux<WindowSystemDTO> getWindwsByIdRol(Long idrol);
}
