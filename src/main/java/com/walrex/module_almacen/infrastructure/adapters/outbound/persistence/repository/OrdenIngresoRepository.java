package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface OrdenIngresoRepository extends ReactiveCrudRepository<OrdenIngresoEntity, Long> {

    @Query("INSERT INTO almacenes.ordeningreso (id_motivo, observacion, fec_ingreso, fec_ref, id_almacen) "+
            "VALUES(:id_motivo, :observacion, :fec_ingreso, :fec_ingreso, :id_almacen)")
    Mono<OrdenIngresoEntity> agregarIngreso(Integer id_motivo, String observacion, LocalDate fec_ingreso, Integer id_almacen);

    @Query("DELETE FROM almacenes.ordeningreso WHERE id_ordeningreso=:idOrdeningreso")
    Mono<Integer> deleteOrdeingresoByIdOrdeningreso(Integer idOrdeingreso);
}
