package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ArticuloRepository extends ReactiveCrudRepository<ArticuloEntity, Long> {

    @Query("SELECT id_articulo, id_familia, id_grupo, cod_articulo, desc_articulo "+
            ", id_medida, id_unidad, id_marca, descripcion, mto_compra, fec_ingreso "+
            ", status, id_unidad_consumo, id_moneda, is_transformacion "+
            "FROM logistica.tbarticulos WHERE cod_articulo=:codArticulo")
    Mono<ArticuloEntity> findOneByCodArticulo(String codArticulo);

    @Query("SELECT id_articulo, id_familia, id_grupo, cod_articulo, desc_articulo"+
            ", id_medida, id_unidad, id_marca, descripcion, mto_compra, fec_ingreso "+
            ", status, id_unidad_consumo, id_moneda, is_transformacion "+
            "FROM logistica.tbarticulos WHERE LOWER(desc_articulo) LIKE :query " +
            "ORDER BY desc_articulo LIMIT :offset OFFSET :size")
    Flux<ArticuloEntity> findByNombreLikeIgnoreCase(String query, int size, int offset);
}
