package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ArticuloCustomRepositoryImpl implements ArticuloCustomRepository {

    private final DatabaseClient databaseClient;

    private static final String SEARCH_WITH_FAMILY_QUERY =
            "SELECT t.id_articulo, t.id_familia, t.id_grupo, t.cod_articulo, t.desc_articulo" +
            ", t.id_medida, t.id_unidad, t.id_marca, t.descripcion, t.mto_compra, t.fec_ingreso" +
            ", t.status, t.id_unidad_consumo, t.id_moneda, t.is_transformacion " +
            "FROM logistica.tbarticulos t " +
            "LEFT OUTER JOIN logistica.tbfamilia t2 ON t2.id_familia = t.id_familia " +
            "WHERE LOWER(t.desc_articulo) LIKE :search AND t2.id_tipo_producto = :idTipoProducto " +
            "ORDER BY t.desc_articulo " +
            "LIMIT :size OFFSET :offset";

    @Override
    public Flux<ArticuloEntity> findByNombreLikeIgnoreCaseAndFamily(String query, int size, int offset, Integer idTipoProducto) {
        return databaseClient.sql(SEARCH_WITH_FAMILY_QUERY)
                .bind("search", query)
                .bind("idTipoProducto", idTipoProducto)
                .bind("size", size)
                .bind("offset", offset)
                .map((row, metadata) -> ArticuloEntity.builder()
                        .id(row.get("id_articulo", Long.class))
                        .id_familia(row.get("id_familia", Integer.class))
                        .id_grupo(row.get("id_grupo", Integer.class))
                        .cod_articulo(row.get("cod_articulo", String.class))
                        .desc_articulo(row.get("desc_articulo", String.class))
                        .id_medida(row.get("id_medida", Integer.class))
                        .id_unidad(row.get("id_unidad", Integer.class))
                        .id_marca(row.get("id_marca", Integer.class))
                        .descripcion(row.get("descripcion", String.class))
                        .mto_compra(row.get("mto_compra", Double.class))
                        .create_at(row.get("fec_ingreso", LocalDate.class))
                        .status(row.get("status", Integer.class))
                        .id_unidad_consumo(row.get("id_unidad_consumo", Integer.class))
                        .id_moneda(row.get("id_moneda", Integer.class))
                        .is_transformacion(row.get("is_transformacion", Boolean.class))
                        .build()
                )
                .all();
    }
}
