package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.DocumentoMovimientoEgresoKardex;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Date;

@Repository
public interface OrdenSalidaRepository extends ReactiveCrudRepository<OrdenSalidaEntity, Long> {

    @Query("INSERT INTO almacenes.ordensalida (id_motivo, id_almacen_origen, id_usuario, fec_entrega, entregado) "+
            " VALUES (:id_motivo, :id_almacen, :id_usuario, :fec_entrega, :entregado)")
    Mono<OrdenSalidaEntity> agregarOrdenSalida(Integer id_motivo, Integer id_almacen, Integer id_usuario, LocalDate fec_entrega, Integer entregado);

    @Query("UPDATE almacenes.ordensalida "+
            "SET fec_entrega=:fec_entrega, id_usuario_entrega=:id_usuario, id_supervisor=:id_supervisor, id_usuario_declara=:id_user_declara, entregado=1 "+
            "WHERE id_ordensalida=:id_ordensalida")
    Mono<Integer> asignarEntregado(Date fech_entrega, Integer idUsuarioEntrega, Integer idSupervisor, Integer idUserDeclara, Integer id_ordensalida);

    @Query("SELECT ord_sal.id_ordensalida, ord_sal.cod_salida, ord_sal.fec_entrega, mot.no_motivo "+
            "FROM almacenes.ordensalida AS ord_sal "+
            "LEFT OUTER JOIN almacenes.tbmotivosingresos AS motsal ON motsal.id_motivos_ingreso=ord_sal.id_motivo "+
            "LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=motsal.id_motivo "+
            "WHERE ord_sal.status=1 AND ord_sal.id_ordensalida=:idOrdenSalida")
    Mono<DocumentoMovimientoEgresoKardex> detalleDocumentoEgreso(Integer idOrdenSalida);

    @Query("UPDATE almacenes.ordensalida "+
        "SET entregado=1 "+
        "WHERE id_ordensalida=:id_ordensalida")
    Mono<Integer> updateForGenerateCodigo(Integer idOrdenSalida);
}
