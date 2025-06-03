package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OrdenSalidaAprobacionPersistenceAdapter extends BaseInventarioAdapter implements OrdenSalidaLogisticaPort {

    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detalleSalidaRepository;
    private final DetailSalidaLoteRepository detalleSalidaLoteRepository;
    private final DetalleInventoryRespository detalleInventoryRespository;
    private final OrdenSalidaEntityMapper ordenSalidaEntityMapper;
    private final DetailSalidaMapper detailSalidaMapper;
    private final KardexRepository kardexRepository;

    // ✅ Constructor explícito (alternativa a @SuperBuilder)
    public OrdenSalidaAprobacionPersistenceAdapter(
            ArticuloAlmacenRepository articuloRepository,                    // Para super()
            OrdenSalidaRepository ordenSalidaRepository,             // Para this
            DetailSalidaRepository detalleSalidaRepository,
            DetailSalidaLoteRepository detalleSalidaLoteRepository,
            DetalleInventoryRespository detalleInventoryRespository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper,
            KardexRepository kardexRepository) {

        super(articuloRepository);  // ✅ Llamada a BaseInventarioAdapter

        // ✅ Asignar campos propios
        this.ordenSalidaRepository = ordenSalidaRepository;
        this.detalleSalidaRepository = detalleSalidaRepository;
        this.detalleSalidaLoteRepository = detalleSalidaLoteRepository;
        this.detalleInventoryRespository = detalleInventoryRespository;
        this.ordenSalidaEntityMapper = ordenSalidaEntityMapper;
        this.detailSalidaMapper = detailSalidaMapper;
        this.kardexRepository = kardexRepository;
    }

    @Override
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        throw new UnsupportedOperationException("Método no implementado para aprobación");
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(Integer idOrden, boolean entregado) {
        log.info("Actualizando estado de entrega para orden: {} a {}", idOrden, entregado);

        Date fechaEntrega = entregado ? new Date() : null;

        return ordenSalidaRepository.asignarEntregado(fechaEntrega, 1, 1, 1, idOrden)
                .map(entity -> ordenSalidaEntityMapper.toDomain(entity))
                .doOnSuccess(orden ->
                        log.info("Estado de entrega actualizado para orden: {}", idOrden));
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        throw new UnsupportedOperationException("Método no implementado para aprobación");
    }

    @Override
    public Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida) {
        return consultarYValidarOrdenSalida(idOrdenSalida)
                .map(ordenEntity -> OrdenEgresoDTO.builder()
                        .id(ordenEntity.getId_ordensalida())
                        .codEgreso(ordenEntity.getCod_salida())
                        .almacenOrigen(Almacen.builder()
                                .idAlmacen(ordenEntity.getId_store_source())
                                .build())
                        .build())
                .doOnSuccess(orden ->
                        log.info("✅ Orden {} preparada para aprobación", orden.getId()));
    }

    /**
     * Método principal para procesar aprobación de un detalle específico
     */
    public Mono<DetalleEgresoDTO> procesarAprobacionDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        log.info("Procesando aprobación de detalle: {} para orden: {}",
                detalle.getId(), ordenSalida.getId());

        return consultarYValidarOrdenSalida(ordenSalida.getId().intValue())
                .then(consultarDetallesOrdenSalida(ordenSalida))
                .flatMap(detallesOrden->validarDetalleEnOrden(detalle, detallesOrden))
                .then(marcarDetalleComoEntregado(detalle))
                .then(procesarEntregaYConversion(detalle, ordenSalida))
                .flatMap(detalleActualizado -> registrarKardexPorDetalle(detalleActualizado, ordenSalida)
                        .then(Mono.just(detalleActualizado)))
                .doOnSuccess(detalleCompletado ->
                        log.info("✅ Aprobación completada para detalle: {}", detalleCompletado.getId()))
                .doOnError(error ->
                        log.error("❌ Error en aprobación de detalle: {}", detalle.getId(), error));
    }

    /**
     * Consulta y valida que la orden de salida exista y no esté entregada
     */
    protected Mono<OrdenSalidaEntity> consultarYValidarOrdenSalida(Integer idOrdenSalida) {
        log.debug("Consultando orden de salida: {}", idOrdenSalida);

        return ordenSalidaRepository.findById(idOrdenSalida.longValue())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        String.format("No se encontró la orden de salida con ID: %d", idOrdenSalida))))
                .flatMap(ordenEntity -> {
                    // ✅ Validar que no esté entregada (status != 1)
                    if (ordenEntity.getStatus() != null && ordenEntity.getStatus() == 0) {
                        String errorMsg = String.format("La orden de salida %d se encuentra actualmente inhabilitada para salida", idOrdenSalida);
                        log.error("❌ {}", errorMsg);
                        return Mono.error(new IllegalStateException(errorMsg));
                    }

                    // ✅ Validar que tenga entregado = 0 (no entregada)
                    if (ordenEntity.getEntregado() != null && ordenEntity.getEntregado() == 1) {
                        String errorMsg = String.format("La orden de salida %d ya fue entregada", idOrdenSalida);
                        log.error("❌ {}", errorMsg);
                        return Mono.error(new IllegalStateException(errorMsg));
                    }
                    log.info("✅ Orden de salida {} válida para aprobación", idOrdenSalida);
                    return Mono.just(ordenEntity);
                })
                .doOnError(error -> log.error("Error al consultar orden de salida {}: {}",
                        idOrdenSalida, error.getMessage()));
    }

    /**
     * Consulta los detalles de una orden de salida
     */
    protected Mono<List<DetalleEgresoDTO>> consultarDetallesOrdenSalida(OrdenEgresoDTO ordenEgreso) {
        if (ordenEgreso == null || ordenEgreso.getId() == null) {
            return Mono.error(new IllegalArgumentException("La orden de egreso no puede ser null"));
        }

        Long idOrdenSalida = ordenEgreso.getId();
        log.debug("Consultando detalles para orden de salida: {}", idOrdenSalida);

        return detalleSalidaRepository.findByIdOrderSalida(idOrdenSalida)
                .doOnNext(detalle -> log.debug("Detalle encontrado: ID={}, Artículo={}, Entregado={}",
                        detalle.getId_detalle_orden(),
                        detalle.getId_articulo(),
                        detalle.getEntregado()))
                .collectList()
                .flatMap(detallesList -> {
                    if (detallesList.isEmpty()) {
                        return Mono.error(new IllegalArgumentException(
                                String.format("No se encontraron detalles para la orden de salida: %d", idOrdenSalida)));
                    }
                    // ✅ Convertir entities a DTOs usando el mapper
                    List<DetalleEgresoDTO> detallesDTO = detallesList.stream()
                            .map(detailSalidaMapper::toDto)
                            .collect(Collectors.toList());

                    log.debug("Se encontraron {} detalles para la orden: {}", detallesDTO.size(), idOrdenSalida);
                    return Mono.just(detallesDTO);
                })
                .doOnError(error -> log.error("Error al consultar detalles de orden {}: {}",
                        idOrdenSalida, error.getMessage()));
    }

    /**
     * Valida que el detalle esté en la orden, no esté entregado y las cantidades coincidan
     */
    protected Mono<Void> validarDetalleEnOrden(DetalleEgresoDTO detalle, List<DetalleEgresoDTO> detallesOrden) {
        Long idDetalle = detalle.getId();
        return Mono.fromCallable(() -> {
                    // ✅ Buscar el detalle en la lista
                    DetalleEgresoDTO detalleEncontrado = detallesOrden.stream()
                            .filter(d -> d.getId().equals(idDetalle))
                            .findFirst()
                            .orElse(null);
                    if (detalleEncontrado == null) {
                        throw new IllegalArgumentException(
                                String.format("El detalle %d no pertenece a esta orden de salida", idDetalle));
                    }
                    // ✅ Validar que no esté entregado
                    if (detalleEncontrado.getEntregado() != null && detalleEncontrado.getEntregado() == 1) {
                        throw new IllegalStateException(
                                String.format("El detalle %d ya está entregado", idDetalle));
                    }
                    // ✅ Validar que las cantidades coincidan
                    if (!detalle.getCantidad().equals(detalleEncontrado.getCantidad())) {
                        throw new IllegalArgumentException(
                                String.format("La cantidad del detalle %d no coincide. Esperada: %s, Recibida: %s",
                                        idDetalle, detalleEncontrado.getCantidad(), detalle.getCantidad()));
                    }
                    log.debug("✅ Detalle {} validado correctamente", idDetalle);
                    return null;
                })
                .then()
                .doOnError(error -> log.error("❌ Error validando detalle {}: {}", idDetalle, error.getMessage()));
    }

    /**
     * Marca el detalle como entregado usando assignedDelivered
     */
    protected Mono<Void> marcarDetalleComoEntregado(DetalleEgresoDTO detalle) {
        return detalleSalidaRepository.assignedDelivered(detalle.getId().intValue())
                .doOnSuccess(updated ->
                        log.debug("✅ Detalle {} marcado como entregado", detalle.getId()))
                .then();
    }

    /**
     * Registra kardex por detalle
     */
    protected Mono<Void> registrarKardexPorDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        if (detalle.getArticulo().getStock().compareTo(BigDecimal.ZERO) < 0) {
            String errorMsg = String.format("Stock insuficiente para artículo %d. Stock actual: %s",
                    detalle.getArticulo().getId(), detalle.getArticulo().getStock());
            log.error("❌ {}", errorMsg);
            return Mono.error(new StockInsuficienteException(errorMsg));
        }

        // ✅ Calcular cantidad convertida
        BigDecimal cantidadConvertida = BigDecimal.valueOf(detalle.getCantidad());
        if (!detalle.getIdUnidad().equals(detalle.getArticulo().getIdUnidadSalida())) {
            Integer valorConv = detalle.getArticulo().getValor_conv();
            if (valorConv == null) {
                String errorMsg = String.format("Valor de conversión no configurado para artículo %d",
                        detalle.getArticulo().getId());
                log.error("❌ {}", errorMsg);
                return Mono.error(new IllegalArgumentException(errorMsg));
            }
            BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, valorConv));
            cantidadConvertida = BigDecimal.valueOf(detalle.getCantidad())
                    .multiply(factorConversion)
                    .setScale(6, RoundingMode.HALF_UP);
        }

        // ✅ Preparar stock inicial
        BigDecimal stockAntesDeSalida = detalle.getArticulo().getStock().add(cantidadConvertida);
        detalle.getArticulo().setStock(stockAntesDeSalida);

        log.debug("📊 Preparando kardex para artículo {}: cantidad_convertida={}, stock_antes_salida={}",
                detalle.getArticulo().getId(), cantidadConvertida, stockAntesDeSalida);

        return detalleSalidaLoteRepository.findByIdDetalleOrden(detalle.getId())
                .switchIfEmpty(Flux.error(new IllegalStateException(
                        String.format("No se encontraron lotes para el detalle %d", detalle.getId()))))
                .concatMap(salidaLote -> registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .then();
    }

    /**
     * Registra kardex por lote específico
     */
    protected Mono<Void> registrarKardexPorLote(DetailSalidaLoteEntity salidaLote,
                                              DetalleEgresoDTO detalle,
                                              OrdenEgresoDTO ordenSalida) {

        BigDecimal saldoStockActual = detalle.getArticulo().getStock();

        Mono<BigDecimal> saldoLoteMono = detalleInventoryRespository.getStockLote(salidaLote.getId_lote())
                .map(lote -> BigDecimal.valueOf(lote.getCantidadDisponible())
                        .add(BigDecimal.valueOf(salidaLote.getCantidad())))
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));

        return saldoLoteMono.flatMap(saldoLoteActual -> {
            BigDecimal cantidadSalida = BigDecimal.valueOf(salidaLote.getCantidad());

            KardexEntity kardexEntity = KardexEntity.builder()
                    .tipo_movimiento(TypeMovimiento.APROBACION_SALIDA_REQUERIMIENTO.getId()) // ✅ Nuevo tipo para aprobación
                    .detalle(String.format("APROBACIÓN SALIDA - ( %s )", ordenSalida.getCodEgreso()))
                    .cantidad(cantidadSalida.negate())
                    .costo(BigDecimal.valueOf(salidaLote.getMonto_consumo()))
                    .valorTotal(BigDecimal.valueOf(salidaLote.getTotal_monto()).negate())
                    .fecha_movimiento(LocalDate.now())
                    .id_articulo(detalle.getArticulo().getId())
                    .id_unidad(detalle.getIdUnidad())
                    .id_unidad_salida(detalle.getIdUnidad())
                    .id_almacen(ordenSalida.getAlmacenOrigen().getIdAlmacen())
                    .saldo_actual(saldoStockActual)
                    .id_documento(ordenSalida.getId().intValue())
                    .id_lote(salidaLote.getId_lote())
                    .id_detalle_documento(detalle.getId().intValue())
                    .saldoLote(saldoLoteActual)
                    .build();

            return kardexRepository.save(kardexEntity)
                    .doOnSuccess(kardex -> {
                        BigDecimal nuevoStock = saldoStockActual.subtract(cantidadSalida);
                        detalle.getArticulo().setStock(nuevoStock);
                        log.info("✅ Kardex registrado: artículo {} lote {} cantidad {} - Stock: {} → {}",
                                detalle.getArticulo().getId(), salidaLote.getId_lote(),
                                cantidadSalida, saldoStockActual, nuevoStock);
                    })
                    .then();
        });
    }
}
