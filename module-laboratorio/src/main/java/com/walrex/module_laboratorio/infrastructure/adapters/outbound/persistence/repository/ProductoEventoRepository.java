package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.ProductoEventoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoEventoRepository extends ReactiveCrudRepository<ProductoEventoEntity, Integer> {

    @Query("""
        SELECT *
        FROM laboratorio.tb_producto_evento
        WHERE (:status IS NULL OR status = :status)
          AND (:search = '' OR LOWER(TRIM(nombre)) LIKE '%' || LOWER(TRIM(:search)) || '%')
        ORDER BY id_producto_evento DESC
        OFFSET :offset LIMIT :limit
        """)
    Flux<ProductoEventoEntity> findAllPaged(String search, long offset, int limit, Integer status);

    @Query("""
        SELECT COUNT(*)
        FROM laboratorio.tb_producto_evento
        WHERE (:status IS NULL OR status = :status)
          AND (:search = '' OR LOWER(TRIM(nombre)) LIKE '%' || LOWER(TRIM(:search)) || '%')
        """)
    Mono<Long> countAll(String search, Integer status);

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM laboratorio.tb_producto_evento
            WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre))
        )
        """)
    Mono<Boolean> existsByNormalizedNombre(String nombre);

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM laboratorio.tb_producto_evento
            WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre))
              AND id_producto_evento <> :id
        )
        """)
    Mono<Boolean> existsByNormalizedNombreExcludingId(String nombre, Integer id);

    @Query("""
        UPDATE laboratorio.tb_producto_evento
        SET status = 0
        WHERE id_producto_evento = :id
        """)
    Mono<Void> logicalDelete(Integer id);
}
