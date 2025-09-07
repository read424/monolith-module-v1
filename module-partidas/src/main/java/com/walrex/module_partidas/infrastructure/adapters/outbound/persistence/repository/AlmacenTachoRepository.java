package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.AlmacenTachoProjection;

import reactor.core.publisher.Flux;

/**
 * Repository reactivo para Almacen Tacho usando proyección
 * Ejecuta consultas SQL complejas con múltiples JOINs
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface AlmacenTachoRepository extends ReactiveCrudRepository<AlmacenTachoProjection, Integer> {

        /**
         * Consulta principal de almacén tacho con SQL complejo
         * Incluye múltiples JOINs y campos calculados
         * 
         * @param idAlmacen ID del almacén
         * @param pageable  Configuración de paginación
         * @return Flux de proyecciones AlmacenTachoProjection
         */
        @Query("""
                SELECT o.id_ordeningreso, o.id_cliente
                , CASE WHEN t.id_tipodoc=3 THEN t.no_razon ELSE TRIM(t.no_apepat || t.no_apemat ||', '|| t.no_nombres)  END AS razon_social
                , t.no_alias
                , o.fec_registro, o.cod_ingreso, d.id_detordeningreso
                , tp.id_partida
                , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END AS cod_partida
                , COUNT(dp.cod_rollo) AS cnt_rollos, tr.cod_receta, tc.no_colores, tc.id_tipo_tenido, tt.desc_tenido, tg.no_gama
                FROM almacenes.ordeningreso o
                LEFT OUTER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
                LEFT OUTER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status = 1
                LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = d.id_comprobante
                LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente
                LEFT OUTER JOIN laboratorio.tb_receta tr ON tr.id_receta = tp.id_receta
                LEFT OUTER JOIN laboratorio.tbcolores tc ON tc.id_colores = tr.id_colores
                LEFT OUTER JOIN laboratorio.tbgamas tg ON tg.id_gama = tc.id_gama
                LEFT OUTER JOIN laboratorio.tbtenido tt ON tt.id_tenido = tc.id_tipo_tenido
                WHERE o.status = 1 AND o.id_almacen = :idAlmacen
                GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                ORDER BY o.id_ordeningreso DESC
                LIMIT :limit OFFSET :offset
                """)
        Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenId(Integer idAlmacen, Integer limit, Integer offset);

        /**
         * Consulta de almacén tacho sin paginación
         * 
         * @param idAlmacen ID del almacén
         * @return Flux de proyecciones AlmacenTachoProjection
         */
        @Query("""
                        SELECT o.id_ordeningreso, o.id_cliente
                        , CASE WHEN t.id_tipodoc=3 THEN t.no_razon ELSE TRIM(t.no_apepat || t.no_apemat ||', '|| t.no_nombres)  END AS razon_social
                        , t.no_alias
                        , o.fec_registro, o.cod_ingreso, d.id_detordeningreso
                        , tp.id_partida
                        , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END AS cod_partida
                        , COUNT(dp.cod_rollo) AS cnt_rollos, tr.cod_receta, tc.no_colores, tc.id_tipo_tenido, tt.desc_tenido, tg.no_gama
                        FROM almacenes.ordeningreso o
                        LEFT OUTER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
                        LEFT OUTER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status = 1
                        LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = d.id_comprobante
                        LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente
                        LEFT OUTER JOIN laboratorio.tb_receta tr ON tr.id_receta = tp.id_receta
                        LEFT OUTER JOIN laboratorio.tbcolores tc ON tc.id_colores = tr.id_colores
                        LEFT OUTER JOIN laboratorio.tbgamas tg ON tg.id_gama = tc.id_gama
                        LEFT OUTER JOIN laboratorio.tbtenido tt ON tt.id_tenido = tc.id_tipo_tenido
                        WHERE o.status=1 AND o.id_almacen = :idAlmacen
                        GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                        ORDER BY o.id_ordeningreso DESC
                        """)
        Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenId(Integer idAlmacen);

        /**
         * Consulta de almacén tacho con búsqueda por código de partida
         * Incluye múltiples JOINs y campos calculados con filtro de búsqueda
         * 
         * @param idAlmacen  ID del almacén
         * @param codPartida Código de partida para búsqueda (opcional)
         * @param limit      Límite de registros
         * @param offset     Offset para paginación
         * @return Flux de proyecciones AlmacenTachoProjection
         */
        @Query("""
                        SELECT o.id_ordeningreso, o.id_cliente
                        , CASE WHEN t.id_tipodoc=3 THEN t.no_razon ELSE TRIM(t.no_apepat || t.no_apemat ||', '|| t.no_nombres)  END AS razon_social
                        , t.no_alias
                        , o.fec_registro, o.cod_ingreso, d.id_detordeningreso
                        , tp.id_partida
                        , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END AS cod_partida
                        , COUNT(dp.cod_rollo) AS cnt_rollos, tr.cod_receta, tc.no_colores, tc.id_tipo_tenido, tt.desc_tenido, tg.no_gama
                        FROM almacenes.ordeningreso o
                        LEFT OUTER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
                        LEFT OUTER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status = 1
                        LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = d.id_comprobante
                        LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente
                        LEFT OUTER JOIN laboratorio.tb_receta tr ON tr.id_receta = tp.id_receta
                        LEFT OUTER JOIN laboratorio.tbcolores tc ON tc.id_colores = tr.id_colores
                        LEFT OUTER JOIN laboratorio.tbgamas tg ON tg.id_gama = tc.id_gama
                        LEFT OUTER JOIN laboratorio.tbtenido tt ON tt.id_tenido = tc.id_tipo_tenido
                        WHERE o.status = 1 AND o.id_almacen = :idAlmacen
                        AND (:codPartida IS NULL OR (tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END) LIKE '%' || :codPartida || '%')
                        GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                        ORDER BY o.id_ordeningreso DESC
                        LIMIT :limit OFFSET :offset
                        """)
        Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenIdAndCodPartida(Integer idAlmacen, String codPartida,
                        Integer limit, Integer offset);
}
