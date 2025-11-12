package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.domain.model.mapper.DetEgresoLoteEntityToItemKardexMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
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
    private final KardexRegistrationStrategy kardexStrategy;
    private final DetEgresoLoteEntityToItemKardexMapper detEgresoLoteEntityToItemKardexMapper;

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        log.info("Guardando orden de salida por transformación: {}", ordenSalida);
        return guardarOrdenPrincipal(ordenSalida)
                .flatMap(this::procesarDetalles)
                .flatMap(this::procesarEntregaYLotes)
                .flatMap(this::actualizarEstadoEntrega)
                .flatMap(this::registrarKardexConLotes)
                .doOnSuccess(orden ->
                        log.info("✅ Orden de salida por transformación completada: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> guardarOrdenPrincipal(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando orden principal de salida");
        Integer id_usuario = 18;
        OrdenSalidaEntity entity = ordenSalidaEntityMapper.toEntity(ordenSalida);
        entity.setEntregado(0); // Inicialmente entregado estará en 0
        entity.setStatus(1); // Activa

        return ordenSalidaRepository.save(entity)
                .map(savedEntity -> {
                    ordenSalida.setId(savedEntity.getId());
                    ordenSalida.setCodEgreso(savedEntity.getCod_salida());
                    return ordenSalida;
                })
                .doOnSuccess(orden ->
                        log.debug("Orden principal guardada con ID: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> procesarDetalles(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando {} detalles de salida detalles: {} ", ordenSalida.getDetalles().size(), ordenSalida.getDetalles());

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> procesarDetalle(detalle, ordenSalida))
                .collectList()
                .map(detallesGuardados -> {
                    ordenSalida.setDetalles(detallesGuardados);
                    return ordenSalida;
                });
    }

    private Mono<DetalleEgresoDTO> procesarDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        log.info("DetalleEgresoDTO {}:", detalle);
        detalle.setIdOrdenEgreso(ordenSalida.getId());
        return guardarDetalleOrdenEgreso(detalle, ordenSalida);
    }

    protected Mono<DetalleEgresoDTO> guardarDetalleOrdenEgreso(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso){
        DetailSalidaEntity detalleEntity = detailSalidaMapper.toEntity(detalle);
        detalleEntity.setEntregado(0);
        log.info("DetailSalidaEntity {}:", detalleEntity);
        return detalleSalidaRepository.save(detalleEntity)
                .map(savedEntity -> {
                    detalle.setId(savedEntity.getId_detalle_orden());
                    return detalle;
                })
                .doOnSuccess(savedDetalle ->
                        log.debug("Detalle guardado: artículo {} con ID {}",
                                savedDetalle.getArticulo(), savedDetalle.getId())
                );
    }

    // ✅ Registrar kardex usando datos de detalle_salida_lote
    protected Mono<OrdenEgresoDTO> registrarKardexConLotes(OrdenEgresoDTO ordenSalida) {
        log.debug("Registrando kardex con información de lotes");

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> registrarKardexPorDetalle(detalle, ordenSalida))
                .then(Mono.just(ordenSalida));
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
        return buscarInfoConversion(detalle, ordenSalida)
                .doOnNext(articuloInfo -> actualizarInfoArticulo(detalle, articuloInfo)) // ✅ Actualizar detalle
                .flatMap(articuloInfo -> validarStockDisponible(detalle, articuloInfo))
                .flatMap(infoConversion->
                    detalleSalidaRepository.assignedDelivered(detalle.getId().intValue())
                        .doOnSuccess(updated->
                                log.debug("✅ Detalle {} marcado como entregado, trigger de lotes ejecutado", detalle.getId())
                        )
                        .then(Mono.just(detalle))
                )
                .doOnSuccess(detalleActualizado->log.debug("✅ Stock actualizado para artículo {}: {}",
                        detalleActualizado.getArticulo().getId(),
                        detalleActualizado.getArticulo().getStock())
                );
    }

    // Método para buscar información de conversión por articulo
    protected Mono<ArticuloInventory> buscarInfoConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso) {
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

    /**
     * Actualiza la información del artículo en el detalle con datos de conversión
     */
    private void actualizarInfoArticulo(DetalleEgresoDTO detalle, ArticuloInventory articuloInfo) {
        if (detalle.getArticulo() == null) {
            detalle.setArticulo(Articulo.builder().id(articuloInfo.getIdArticulo()).build());
        }
        // ✅ Setear información de conversión desde ArticuloInventory
        Articulo articulo =detalle.getArticulo();

        articulo.setIdUnidad(articuloInfo.getIdUnidad());
        articulo.setIdUnidadSalida(articuloInfo.getIdUnidadConsumo());
        articulo.setIs_multiplo(articuloInfo.getIsMultiplo());
        articulo.setValor_conv(articuloInfo.getValorConv());
        articulo.setStock(articuloInfo.getStock());
        log.debug("✅ Información de artículo actualizada - Stock: {}, Unidad: {}, Conversión: {}",
                articulo.getStock(), articulo.getIdUnidad(), articulo.getValor_conv());
    }

    private Mono<ArticuloInventory> validarStockDisponible(DetalleEgresoDTO detalle, ArticuloInventory articuloInfo){
        BigDecimal stockDisponible = detalle.getArticulo().getStock();
        BigDecimal cantidadSalidaSolicitada= BigDecimal.valueOf(detalle.getCantidad());
        if(stockDisponible==null){
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No se encontró stock disponible para el artículo %d", detalle.getArticulo().getId())));
        }
        if(!detalle.getIdUnidad().equals(detalle.getArticulo().getId())){
            Integer valorConv = detalle.getArticulo().getValor_conv();
            if (valorConv == null) {
                String errorMsg = String.format("Valor de conversión no configurado para artículo %d",
                        detalle.getArticulo().getId());
                log.error("❌ {}", errorMsg);
                return Mono.error(new IllegalArgumentException(errorMsg));
            }
            BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, valorConv));
            cantidadSalidaSolicitada=cantidadSalidaSolicitada
                    .multiply(factorConversion)
                    .setScale(6, RoundingMode.HALF_UP);
        }

        if(stockDisponible.compareTo(cantidadSalidaSolicitada)<0){
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Stock insuficiente. Disponible: %s, Solicitado: %s",
                            stockDisponible, cantidadSalidaSolicitada)
            ));
        }
        log.info("✅ Stock validado - Disponible: {}, Solicitado: {}", stockDisponible, cantidadSalidaSolicitada);
        return Mono.just(articuloInfo);
    }


    protected Mono<Void> registrarKardexPorDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        if (detalle.getArticulo().getStock().compareTo(BigDecimal.ZERO)==0) {
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

        // ✅ Validar que cantidad convertida no sea mayor o igual al stock
        if (cantidadConvertida.compareTo(detalle.getArticulo().getStock()) > 0) {
            String errorMsg = String.format("Cantidad solicitada (%s) es mayor o igual al stock disponible (%s) para artículo %d",
                    cantidadConvertida,
                    detalle.getArticulo().getStock(),
                    detalle.getArticulo().getId());
            log.error("❌ {}", errorMsg);
            return Mono.error(new StockInsuficienteException(errorMsg));
        }

        log.debug("📊 Preparando kardex para artículo {}: stock_inicial={}, cantidad_convertida={}, stock_antes_salida={}",
                detalle.getArticulo().getId(),
                detalle.getArticulo().getStock(),
                cantidadConvertida,
                detalle.getArticulo().getStock());
        return detalleSalidaLoteRepository.findByIdDetalleOrden(detalle.getId())
                .switchIfEmpty(Flux.error(new IllegalStateException(
                        String.format("No se encontraron lotes para el detalle %d", detalle.getId()))))
                .concatMap(salidaLote -> registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .then();
    }

    protected Mono<Void> registrarKardexPorLote(DetailSalidaLoteEntity salidaLote,
                                                DetalleEgresoDTO detalle,
                                                OrdenEgresoDTO ordenSalida) {

        // ✅ Mapear base y completar información
        ItemKardexDTO itemKardexDTO = configurarItemKardex(salidaLote, detalle, ordenSalida);

        return consultarInventarioLote(salidaLote.getId_lote())
                .flatMap(lote_inventario->{
                    itemKardexDTO.setSaldoLote(BigDecimal.valueOf(lote_inventario.getCantidadDisponible()).add(BigDecimal.valueOf(salidaLote.getCantidad())));
                    log.info("itemKardexDTO: {}", itemKardexDTO);
                    return kardexStrategy.registrarKardex(itemKardexDTO)
                            .doOnSuccess(kardexGuardado -> log.info("✅ Kardex registrado con ID: {}, item: {}", kardexGuardado.getId_kardex(), kardexGuardado))
                            .doOnError(error -> log.error("❌ Error registrando kardex: {}", error.getMessage()))
                            .then();
                });
    }

    private ItemKardexDTO configurarItemKardex(DetailSalidaLoteEntity salidaLote,
                                               DetalleEgresoDTO detalle,
                                               OrdenEgresoDTO ordenSalida) {
        //mapear
        ItemKardexDTO itemKardexDTO = detEgresoLoteEntityToItemKardexMapper.toItemKardex(salidaLote);
        itemKardexDTO.setFechaMovimiento(ordenSalida.getFecRegistro());

        itemKardexDTO.setIdArticulo(detalle.getArticulo().getId());
        itemKardexDTO.setIdUnidad(detalle.getIdUnidad());

        String descripcionKardex = String.format("SALIDA TRANSFORMACION - (%s) ", ordenSalida.getCodEgreso());
        itemKardexDTO.setTypeKardex(TypeMovimiento.APROBACION_SALIDA_REQUERIMIENTO.getId());
        itemKardexDTO.setIdAlmacen(ordenSalida.getAlmacenOrigen().getIdAlmacen());

        itemKardexDTO.setIdUnidadSalida(detalle.getArticulo().getIdUnidadSalida());
        itemKardexDTO.setDescripcion(descripcionKardex);

        // ✅ Consultar saldo actual del artículo
        itemKardexDTO.setSaldoStock(detalle.getArticulo().getStock());
        itemKardexDTO.setIdLote(salidaLote.getId_lote());

        itemKardexDTO.setValorUnidad(BigDecimal.valueOf(salidaLote.getMonto_consumo()));
        itemKardexDTO.setValorTotal(BigDecimal.valueOf(salidaLote.getTotal_monto()));
        return itemKardexDTO;
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(OrdenEgresoDTO ordenEgresoDTO) {
        log.info("Actualizando estado de entrega para orden: {} ", ordenEgresoDTO.getId());

        Date fechaEntrega = new Date();

        return ordenSalidaRepository.asignarEntregado(fechaEntrega, ordenEgresoDTO.getIdUsuarioEntrega(), ordenEgresoDTO.getIdSupervisor(), ordenEgresoDTO.getIdUsuarioEntrega(), ordenEgresoDTO.getId().intValue())
                .flatMap(entityFromUpdate ->
                        ordenSalidaRepository.findById(ordenEgresoDTO.getId())
                                .map(entityWithTrigger -> {
                                    log.info("🔍 Código generado por trigger: {}", entityWithTrigger.getCod_salida());
                                    ordenEgresoDTO.setCodEgreso(entityWithTrigger.getCod_salida());
                                    ordenEgresoDTO.setEntregado(entityWithTrigger.getEntregado());
                                    return ordenEgresoDTO;
                                })
                                .switchIfEmpty(Mono.error(new IllegalStateException(
                                        "Orden no encontrada después del update: " + ordenEgresoDTO.getId())))
                )
                .switchIfEmpty(
                        consultarOrdenActual(ordenEgresoDTO.getId())
                                .flatMap(ordenActual -> {
                                    if (ordenActual.getEntregado() == 1) {
                                        // ✅ Ya estaba entregada - OK
                                        log.warn("⚠️ Orden {} ya estaba entregada por otro proceso", ordenEgresoDTO.getId());
                                        ordenEgresoDTO.setCodEgreso(ordenActual.getCod_salida());
                                        ordenEgresoDTO.setEntregado(1);
                                        return Mono.just(ordenEgresoDTO);
                                    } else {
                                        // ❌ Error inesperado
                                        return Mono.error(new IllegalStateException(
                                                "No se pudo actualizar la orden " + ordenEgresoDTO.getId()));
                                    }
                                })
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Orden no encontrada: " + ordenEgresoDTO.getId())))
                )
                .doOnSuccess(orden ->
                        log.info("Estado de entrega actualizado para orden: {}", ordenEgresoDTO.getId()));
    }

    // ✅ AGREGAR ESTE MÉTODO TAMBIÉN
    private Mono<OrdenSalidaEntity> consultarOrdenActual(Long idOrden) {
        return ordenSalidaRepository.findById(idOrden);
    }

    /**
     * Consulta el inventario completo del lote
     */
    private Mono<DetalleInventaryEntity> consultarInventarioLote(Integer idLote) {
        log.debug("🔍 Consultando inventario completo para lote: {}", idLote);

        return detalleInventoryRespository.getStockLote(idLote)
                .doOnNext(inventario ->
                        log.debug("✅ Inventario encontrado para lote {}: cantidad={}",
                                idLote, inventario.getCantidadDisponible()))
                .switchIfEmpty(Mono.error(new StockInsuficienteException(
                        String.format("No se encontró inventario para el lote %d", idLote))));
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