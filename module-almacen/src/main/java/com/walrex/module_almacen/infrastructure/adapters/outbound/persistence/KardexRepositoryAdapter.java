package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRepositoryPort;
import com.walrex.module_almacen.common.utils.RowMapperHelper;
import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexDetalleMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexEnriquecidoMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.KardexDetalleProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexRepositoryAdapter implements KardexRepositoryPort {
    private final DatabaseClient databaseClient;
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final RowMapperHelper rowMapperHelper;
    private final KardexEnriquecidoMapper kardexEnriquecidoMapper;
    private final KardexDetalleMapper kardexDetalleMapper;

    @Override
    public Mono<List<KardexArticuloDTO>> consultarMovimientosKardex(CriteriosBusquedaKardex criterios) {
        log.info("🔍 Consultando movimientos kardex para: {}", criterios);

        return consultarKardexBase(criterios)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::enriquecerRegistroKardex)
                .collectList()
                .map(this::agruparPorArticulo);
    }

    private Mono<List<KardexDetalleProjection>> consultarKardexBase(CriteriosBusquedaKardex criterios) {
        String query = buildDynamicQuery(criterios);
        log.info("🔧 Query construido: {}", query);

        var spec = databaseClient.sql(query);
        spec = bindParameters(spec, criterios);

        return spec.map((row, metadata) -> mapRowToProjection(row))
                .all()
                .collectList()
                .doOnNext(results -> log.info("📊 {} registros encontrados", results.size()));
    }

    private Mono<KardexDetalleEnriquecido> enriquecerRegistroKardex(KardexDetalleProjection kardex) {
        KardexDetalleEnriquecido enriquecido = kardexEnriquecidoMapper.toEnriquecido(kardex);
        return switch (kardex.getTipoKardex()) {
            case 1 -> consultarDetalleIngreso(kardex.getIdDocumento())
                    .doOnNext(detalle -> enriquecido.setCodigoDocumento(detalle.getCod_ingreso()))
                    .thenReturn(enriquecido);
            case 2 -> consultarDetalleSalida(kardex.getIdDocumento())
                    .doOnNext(detalle -> enriquecido.setCodigoDocumento(detalle.getCod_salida()))
                    .thenReturn(enriquecido);
            default -> {
                enriquecido.setCodigoDocumento(null);
                yield Mono.just(enriquecido);
            }
        };
    }

    private Mono<OrdenIngresoEntity> consultarDetalleIngreso(Integer idDocumento) {
        return ordenIngresoRepository.findById(idDocumento.longValue());
    }

    private Mono<OrdenSalidaEntity> consultarDetalleSalida(Integer idDocumento) {
        return ordenSalidaRepository.findById(idDocumento.longValue());
    }

    private List<KardexArticuloDTO> agruparPorArticulo(List<KardexDetalleEnriquecido> detallesEnriquecidos) {
        return detallesEnriquecidos.stream()
                .collect(Collectors.groupingBy(KardexDetalleEnriquecido::getIdArticulo))
                .entrySet().stream()
                .map(entry -> construirReporteArticulo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private KardexArticuloDTO construirReporteArticulo(Integer idArticulo, List<KardexDetalleEnriquecido> detalles) {

        // 1. precioAvg: suma de precios / cantidad de items
        BigDecimal precioPromedio = detalles.stream()
                .map(KardexDetalleEnriquecido::getValorUnidad)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(detalles.size()), RoundingMode.HALF_UP);

        // 2. totalValorizado: type_kardex 1 suma, type_kardex 2 resta
        BigDecimal totalValorizado = detalles.stream()
                .map(detalle->{
                    BigDecimal valor = detalle.getValorTotal();
                    return detalle.getTipoKardex()==1? valor: valor.negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal stockDisponible = detalles.get(detalles.size()-1).getSaldoStock();

        List<KardexDetalleDTO> detallesReporte = detalles.stream()
                .map(kardexDetalleMapper::toDTO)
                .collect(Collectors.toList());

        return KardexArticuloDTO.builder()
                .idArticulo(idArticulo)
                .descArticulo(detalles.get(0).getDescArticulo())
                .precioAvg(precioPromedio)
                .totalValorizado(totalValorizado)
                .stockDisponible(stockDisponible)
                .detalles(detallesReporte)
                .build();
    }

    private KardexDetalleProjection mapRowToProjection(Row row) {
        return rowMapperHelper.mapRow(row, KardexDetalleProjection.class);
    }

    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec spec, CriteriosBusquedaKardex criterios) {
        if (criterios.getIdArticulo() != null) {
            spec = spec.bind("idArticulo", criterios.getIdArticulo());
        }
        if (criterios.getIdAlmacen() != null) {
            spec = spec.bind("idAlmacen", criterios.getIdAlmacen());
        }
        if (criterios.getFechaInicio() != null) {
            spec = spec.bind("fechaInicio", criterios.getFechaInicio());
        }
        if (criterios.getFechaFin() != null) {
            spec = spec.bind("fechaFin", criterios.getFechaFin());
        }
        return spec;
    }


    private String buildDynamicQuery(CriteriosBusquedaKardex criterios) {
        StringBuilder whereClause = new StringBuilder("WHERE k.tipo_kardex IN (1, 2) ");

        // Agregar condiciones según criterios disponibles
        if (criterios.getIdArticulo() != null) {
            whereClause.append("AND k.id_articulo = :idArticulo ");
        }

        if (criterios.getIdAlmacen() != null) {
            whereClause.append("AND k.id_almacen = :idAlmacen ");
        }

        if (criterios.getFechaInicio() != null && criterios.getFechaFin() != null) {
            whereClause.append("AND k.fecha_movimiento BETWEEN :fechaInicio AND :fechaFin ");
        }

        return String.format(BASE_QUERY_TEMPLATE, whereClause.toString());
    }

    private static final String BASE_QUERY_TEMPLATE = """
       SELECT k.id_kardex, k.tipo_kardex, k.detalle, k.cantidad, k.valor_unidad, k.valor_total
       , k.fecha_movimiento,  k.id_articulo, art.desc_articulo, k.status
       , k.id_unidad, unidad.abrev_unidad, unidad.desc_unidad
       , k.id_unidad_salida, unidad_salida.abrev_unidad AS abrev_salida, unidad_salida.desc_unidad AS desc_unidad_salida 
       , k.id_almacen, k.saldo_stock, k.saldo_lote, k.id_lote
       , k.id_documento, k.id_detalle_documento
       FROM almacenes.kardex AS k
       LEFT OUTER JOIN logistica.tbarticulos AS art ON art.id_articulo=k.id_articulo
       LEFT OUTER JOIN logistica.tbunidad AS unidad ON unidad.id_unidad=k.id_unidad
       LEFT OUTER JOIN logistica.tbunidad AS unidad_salida ON unidad_salida.id_unidad=k.id_unidad_salida
       %s
       ORDER BY k.id_articulo ASC, k.fecha_movimiento ASC
        """;
}
