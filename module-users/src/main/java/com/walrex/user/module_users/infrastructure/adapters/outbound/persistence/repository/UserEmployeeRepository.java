package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.projection.UserEmployee;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserEmployeeRepository extends ReactiveCrudRepository<UserEntity, Long> {

    @Query("SELECT t.id_usuario, t.id_empleado, t.no_usuario, t.il_estado, t.idrol_sistema, t.state_default "+
            ", t2.id_tipoper, t2.no_apepat, t2.no_nombres "+
            ", tp.id_det_personal, tp.id_area, t3.no_area, tp.status, tp.id_status "+
            "FROM seguridad.tbusuarios t "+
            "LEFT OUTER JOIN rrhh.tbpersonal t2 ON t2.id_personal = t.id_empleado "+
            "INNER JOIN rrhh.tcdet_personal tp ON tp.id_personal = t2.id_personal "+
            "LEFT OUTER JOIN rrhh.tbarea t3 ON t3.id_area = tp.id_area "+
            "INNER JOIN rrhh.tbstatus ts ON ts.id_status = tp.id_status "+
            "WHERE ts.id_status!=13 AND t.no_usuario=:username")
    Mono<UserEmployee> findEmployeeByUserName(String username);
}
