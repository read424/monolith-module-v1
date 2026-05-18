package com.walrex.module_machines.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_machines.domain.model.Maquina;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class MaquinaRepositoryImpl implements MaquinaRepository {

    private final DatabaseClient db;

    private static final String FIND_ALL_BY_UBICACION_QUERY = """
            SELECT t2.id_maquina
                 , t2.id_ubicacion
                 , t2.desc_maq
            FROM catalogo.tbubicacion t
            INNER JOIN catalogo.tbmaquina t2 ON t2.id_ubicacion = t.id_ubicacion AND t2.status = 1
            WHERE t.id_ubicacion = :idUbicacion
            ORDER BY t2.desc_maq ASC
            """;

    @Override
    public Flux<Maquina> findAllByUbicacion(Integer idUbicacion) {
        return db.sql(FIND_ALL_BY_UBICACION_QUERY)
                .bind("idUbicacion", idUbicacion)
                .map((row, meta) -> Maquina.builder()
                        .idMaquina(row.get("id_maquina", Integer.class))
                        .idUbicacion(row.get("id_ubicacion", Integer.class))
                        .descMaq(row.get("desc_maq", String.class))
                        .build())
                .all();
    }
}
