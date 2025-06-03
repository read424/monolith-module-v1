package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class OrdenSalidaTransformacionPersistenceAdapter implements OrdenSalidaLogisticaPort {
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detalleSalidaRepository;
    private final ArticuloAlmacenRepository articuloRepository;
    private final DetailSalidaLoteRepository detalleSalidaLoteRepository;
    private final DetalleInventoryRespository detalleInventoryRespository;
    private final OrdenSalidaEntityMapper ordenSalidaEntityMapper;
    private final DetailSalidaMapper detailSalidaMapper;
    private final KardexRepository kardexRepository;

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        log.info("Guardando orden de salida por transformación: {}", ordenSalida.getMotivo().getIdMotivo());
        return guardarOrdenPrincipal(ordenSalida)
                .flatMap(this::procesarDetalles)
                .flatMap(this::procesarEntregaYLotes)
                .flatMap(this::registrarKardexConLotes)
                .flatMap(orden -> actualizarEstadoEntrega(orden.getId().intValue(), true))
                .doOnSuccess(orden ->
                        log.info("✅ Orden de salida por transformación completada: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> guardarOrdenPrincipal(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando orden principal de salida");
        Integer id_usuario = 18;
        OrdenSalidaEntity entity = ordenSalidaEntityMapper.toEntity(ordenSalida);
        entity.setEntregado(0); // Inicialmente entregado estará en 0
        entity.setCreate_at(OffsetDateTime.now());
        entity.setFec_entrega(null);
        entity.setId_user_entrega(null);
        entity.setId_usuario(null);
        entity.setStatus(1); // Activa

        return ordenSalidaRepository.save(entity)
                .map(savedEntity -> {
                    ordenSalida.setId(savedEntity.getId_ordensalida());
                    ordenSalida.setCodEgreso(savedEntity.getCod_salida());
                    return ordenSalida;
                })
                .doOnSuccess(orden ->
                        log.debug("Orden principal guardada con ID: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> procesarDetalles(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando {} detalles de salida", ordenSalida.getDetalles().size());

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> procesarDetalle(detalle, ordenSalida))
                .collectList()
                .map(detallesGuardados -> {
                    ordenSalida.setDetalles(detallesGuardados);
                    return ordenSalida;
                });
    }

    private Mono<DetalleEgresoDTO> procesarDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        detalle.setIdOrdenEgreso(ordenSalida.getId());
        return guardarDetalleOrdenEgreso(detalle, ordenSalida);
    }

    protected Mono<DetalleEgresoDTO> guardarDetalleOrdenEgreso(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso){
        DetailSalidaEntity detalleEntity = detailSalidaMapper.toEntity(detalle);

        return detalleSalidaRepository.save(detalleEntity)
                .map(savedEntity -> {
                    detalle.setId(savedEntity.getId_detalle_orden());
                    detalle.setIdOrdenEgreso(ordenEgreso.getId());
                    return detalle;
                })
                .doOnSuccess(savedDetalle ->
                        log.debug("Detalle guardado: artículo {} con ID {}",
                                savedDetalle.getArticulo().getId(), savedDetalle.getId()));
    }

    // ✅ Nuevo método para procesar entrega y activar triggers
    private Mono<OrdenEgresoDTO> procesarEntregaYLotes(OrdenEgresoDTO ordenSalida) {
        log.debug("Procesando entrega y activando triggers de lotes");

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle ->procesarEntregaYConversion(detalle, ordenSalida))
                .then(Mono.just(ordenSalida));
    }

    // ✅ Método auxiliar para procesar cada detalle
    protected Mono<DetalleEgresoDTO> procesarEntregaYConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        return detalleSalidaRepository.assignedDelivered(detalle.getId().intValue())
                .doOnSuccess(updated ->
                        log.debug("Detalle {} marcado como entregado, trigger de lotes ejecutado", detalle.getId()))
                .then(buscarInfoConversion(detalle, ordenSalida))
                .flatMap(infoConversion -> aplicarConversion(detalle, infoConversion))
                .doOnSuccess(detalleActualizado ->
                        log.debug("✅ Stock actualizado para artículo {}: {}",
                                detalleActualizado.getArticulo().getId(),
                                detalleActualizado.getArticulo().getStock()));
    }

    // Método para buscar información de conversión por articulo
    protected Mono<ArticuloEntity> buscarInfoConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso) {
        // ✅ Validar que detalle no sea null
        if (detalle == null) {
            return Mono.error(new IllegalArgumentException("El detalle no puede ser null"));
        }

        // ✅ Validar que ordenEgreso no sea null
        if (ordenEgreso == null) {
            return Mono.error(new IllegalArgumentException("La orden de egreso no puede ser null"));
        }

        // ✅ Validar que almacenOrigen no sea null
        if (ordenEgreso.getAlmacenOrigen() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("Almacén origen no puede ser null para la orden %d", ordenEgreso.getId())));
        }

        // ✅ Validar que idAlmacen no sea null
        if (ordenEgreso.getAlmacenOrigen().getIdAlmacen() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("ID de almacén origen no puede ser null para la orden %d", ordenEgreso.getId())));
        }

        // ✅ Validar que artículo no sea null
        if (detalle.getArticulo() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("Artículo no puede ser null para el detalle %d", detalle.getId())));
        }

        // ✅ Validar que ID de artículo no sea null
        if (detalle.getArticulo().getId() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("ID de artículo no puede ser null para el detalle %d", detalle.getId())));
        }

        Integer idAlmacen = ordenEgreso.getAlmacenOrigen().getIdAlmacen();
        Integer idArticulo = detalle.getArticulo().getId();

        log.debug("🔍 Buscando información de conversión para artículo {} en almacén {}", idArticulo, idAlmacen);
        return articuloRepository.getInfoConversionArticulo(
                        idAlmacen,
                        idArticulo
                )
                .doOnNext(info -> log.info("✅ Información de conversión encontrada: {}", info))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "No se encontró información de conversión para el artículo: " +
                                        detalle.getArticulo().getId()
                        ))
                );
    }

    // Método para aplicar conversión
    protected Mono<DetalleEgresoDTO> aplicarConversion(DetalleEgresoDTO detalle, ArticuloEntity infoConversion) {
        // ✅ Validar que idUnidad no sea null
        if (detalle.getIdUnidad() == null) {
            String errorMsg = String.format("ID de unidad no puede ser null para el detalle %d del artículo %d",
                    detalle.getId(),
                    detalle.getArticulo().getId());
            log.error("❌ {}", errorMsg);
            return Mono.error(new IllegalArgumentException(errorMsg));
        }
        if (!detalle.getIdUnidad().equals(infoConversion.getIdUnidadConsumo())) {
            detalle.getArticulo().setIdUnidadSalida(infoConversion.getIdUnidadConsumo());
            detalle.getArticulo().setIs_multiplo(infoConversion.getIsMultiplo());
            detalle.getArticulo().setValor_conv(infoConversion.getValorConv());
            detalle.getArticulo().setStock(infoConversion.getStock());
        } else {
            detalle.getArticulo().setIdUnidadSalida(detalle.getIdUnidad());
        }
        return Mono.just(detalle);
    }

    // ✅ Registrar kardex usando datos de detalle_salida_lote
    protected Mono<OrdenEgresoDTO> registrarKardexConLotes(OrdenEgresoDTO ordenSalida) {
        log.debug("Registrando kardex con información de lotes");

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> registrarKardexPorDetalle(detalle, ordenSalida))
                .then(Mono.just(ordenSalida));
    }

    protected Mono<Void> registrarKardexPorDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        if (detalle.getArticulo().getStock().compareTo(BigDecimal.ZERO) < 0) {
            String errorMsg = String.format("Stock insuficiente para artículo %d. Stock actual: %s",
                    detalle.getArticulo().getId(),
                    detalle.getArticulo().getStock());
            log.error("❌ {}", errorMsg);
            return Mono.error(new StockInsuficienteException(errorMsg));
        }
        // ✅ Calcular cantidad convertida
        BigDecimal cantidadConvertida = BigDecimal.valueOf(detalle.getCantidad());
        if (!detalle.getIdUnidad().equals(detalle.getArticulo().getIdUnidadSalida())) {
            // ✅ Validar que valor_conv no sea null
            Integer valorConv = detalle.getArticulo().getValor_conv();
            if (valorConv == null) {
                String errorMsg = String.format("Valor de conversión no configurado para artículo %d",
                        detalle.getArticulo().getId());
                log.error("❌ {}", errorMsg);
                return Mono.error(new IllegalArgumentException(errorMsg));
            }
            BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, valorConv));
            cantidadConvertida = BigDecimal.valueOf(detalle.getCantidad()).multiply(factorConversion).setScale(6, RoundingMode.HALF_UP);
        }

        // ✅ Preparar stock inicial (stock actual + cantidad que va a salir)
        BigDecimal stockAntesDeSalida = detalle.getArticulo().getStock().add(cantidadConvertida);//20000+60000=80000
        detalle.getArticulo().setStock(stockAntesDeSalida);//80000

        log.debug("📊 Preparando kardex para artículo {}: stock_inicial={}, cantidad_convertida={}, stock_antes_salida={}",
                detalle.getArticulo().getId(),
                detalle.getArticulo().getStock().subtract(cantidadConvertida),
                cantidadConvertida,
                stockAntesDeSalida);
        return detalleSalidaLoteRepository.findByIdDetalleOrden(detalle.getId())
            .concatMap(salidaLote -> registrarKardexPorLote(salidaLote, detalle, ordenSalida))
            .then();
    }

    protected Mono<Void> registrarKardexPorLote(DetailSalidaLoteEntity salidaLote,
                                                DetalleEgresoDTO detalle,
                                                OrdenEgresoDTO ordenSalida) {

        // ✅ Consultar saldo actual del artículo
        BigDecimal saldoStockActual = detalle.getArticulo().getStock();//

        // ✅ Consultar saldo actual del lote, en este momento el disparador ya desconto la cantidad total previa
        Mono<BigDecimal> saldoLoteMono = detalleInventoryRespository.getStockLote(salidaLote.getId_lote())
                .map(lote -> BigDecimal.valueOf(lote.getCantidadDisponible()).add(BigDecimal.valueOf(salidaLote.getCantidad())))
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));

        return saldoLoteMono
                .flatMap(saldoLoteActual -> {
                    BigDecimal cantidadSalida = BigDecimal.valueOf(salidaLote.getCantidad());

                    // ✅ Crear registro de kardex
                    KardexEntity kardexEntity = KardexEntity.builder()
                            .tipo_movimiento(TypeMovimiento.INTERNO_TRANSFORMACION.getId())
                            .detalle(String.format("SALIDA TRANSFORMACIÓN - ( %s )", ordenSalida.getCodEgreso()))
                            .cantidad(cantidadSalida.negate())
                            .costo(BigDecimal.valueOf(salidaLote.getMonto_consumo()))
                            .valorTotal(BigDecimal.valueOf(salidaLote.getTotal_monto()).negate())
                            .fecha_movimiento(LocalDate.now())
                            .id_articulo(detalle.getArticulo().getId())
                            .id_unidad(detalle.getIdUnidad())
                            .id_unidad_salida(detalle.getIdUnidad())
                            .id_almacen(ordenSalida.getAlmacenOrigen().getIdAlmacen())
                            .saldo_actual(saldoStockActual) // ✅ Stock ANTES de esta salida específica
                            .id_documento(ordenSalida.getId().intValue())
                            .id_lote(salidaLote.getId_lote())
                            .id_detalle_documento(detalle.getId().intValue())
                            .saldoLote(saldoLoteActual)
                            .build();

                    return kardexRepository.save(kardexEntity)
                            .doOnSuccess(kardex -> {
                                // ✅ Actualizar stock para próxima iteración
                                BigDecimal nuevoStock = saldoStockActual.subtract(cantidadSalida);
                                detalle.getArticulo().setStock(nuevoStock);
                                log.info("✅ Kardex registrado: artículo {} lote {} cantidad {} - Stock: {} → {}",
                                        detalle.getArticulo().getId(),
                                        salidaLote.getId_lote(),
                                        cantidadSalida,
                                        saldoStockActual,
                                        nuevoStock);
                            })
                            .then();
                });
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(Integer idOrden, boolean entregado) {
        log.info("Actualizando estado de entrega para orden: {} a {}", idOrden, entregado);

        Date fechaEntrega = entregado ? new Date() : null;

        return ordenSalidaRepository.asignarEntregado(fechaEntrega, 1, 1, 1, idOrden)
                .flatMap(entity ->
                        // Actualizar detalles como entregados
                        actualizarDetallesEntregados(idOrden.longValue())
                                .then(Mono.just(ordenSalidaEntityMapper.toDomain(entity)))
                )
                .doOnSuccess(orden ->
                        log.info("Estado de entrega actualizado para orden: {}", idOrden));
    }

    private Mono<Void> actualizarDetallesEntregados(Long idOrdenSalida) {
        // Aquí podrías actualizar los detalles individuales si es necesario
        // Por ahora, el trigger de BD se encarga de esto
        return Mono.empty();
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        log.info("Procesando salida por lotes para orden: {}", ordenSalida.getId());

        // La lógica de lotes la manejan los triggers de BD por ahora
        // Aquí podrías implementar la lógica FIFO si decides migrarla a Java

        return Mono.just(ordenSalida)
                .doOnSuccess(orden ->
                        log.info("Salida por lotes procesada para orden: {}", orden.getId()));
    }

    @Override
    public Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida) {
        return null;
    }
}