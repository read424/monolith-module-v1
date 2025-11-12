package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ArticuloAlmacenRepository extends ReactiveCrudRepository<ArticuloEntity, Integer> {

    @Query("SELECT art.id_articulo, COALESCE(conv_si.valor_conv, 0) AS valor_conv " +
            ", COALESCE(conv_si.is_multiplo, '0') AS is_multiplo, fam.id_tipo_producto " +
            ", art.id_unidad, art.id_unidad_consumo, inv.stock " +
            "FROM logistica.tbarticulos AS art " +
            "LEFT OUTER JOIN logistica.conversion_si AS conv_si ON conv_si.id_uni_medida=art.id_unidad AND conv_si.id_uni_medida_conv=art.id_unidad_consumo " +
            "LEFT OUTER JOIN logistica.tbfamilia AS fam ON fam.id_familia=art.id_familia " +
            "LEFT OUTER JOIN almacenes.inventario AS inv ON inv.id_articulo=art.id_articulo AND inv.id_almacen=:idAlmacen " +
            "WHERE art.id_articulo=:idArticulo")
    Mono<ArticuloInventory> getInfoConversionArticulo(Integer idAlmacen, Integer idArticulo);
}
