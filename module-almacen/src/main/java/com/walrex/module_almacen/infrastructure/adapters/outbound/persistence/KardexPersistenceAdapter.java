package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.GuardarKardexPort;
import com.walrex.module_almacen.domain.Kardex;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexPersistenceAdapter implements GuardarKardexPort {
    private final KardexRepository kardexRepository;

    @Override
    public Mono<Kardex> guardarKardex(Kardex kardex) {
        log.info("Guardando movimiento en kardex para artículo: {} en almacén: {}",
                kardex.getIdArticulo(), kardex.getIdAlmacen());

        // Convertimos de dominio a entidad
        KardexEntity entity = KardexEntity.builder()
                .tipo_movimiento(kardex.getTipoKardex())
                .detalle(kardex.getDetalle())
                .cantidad(kardex.getCantidad())
                .costo(kardex.getValorUnidad())
                .valorTotal(kardex.getValorTotal())
                .fecha_movimiento(kardex.getFechaMovimiento())
                .id_articulo(kardex.getIdArticulo())
                .id_unidad(kardex.getIdUnidad())
                .id_unidad_salida(kardex.getIdUnidadSalida())
                .id_almacen(kardex.getIdAlmacen())
                .saldo_actual(kardex.getSaldoStock())
                .id_documento(kardex.getIdDocumento())
                .id_detalle_documento(kardex.getIdDetalleDocumento())
                .id_lote(kardex.getIdLote())
                .saldoLote(kardex.getSaldoLote())
                .build();

        // Guardamos y convertimos de vuelta a dominio
        return kardexRepository.save(entity)
            .map(savedEntity -> Kardex.builder()
                .idKardex(savedEntity.getId_kardex().intValue())
                .idArticulo(savedEntity.getId_articulo())
                .idAlmacen(savedEntity.getId_almacen())
                .tipoKardex(savedEntity.getTipo_movimiento())
                .idDocumento(savedEntity.getId_documento())
                .idDetalleDocumento(savedEntity.getId_detalle_documento())
                .fechaMovimiento(savedEntity.getFecha_movimiento())
                .cantidad(savedEntity.getCantidad())
                .saldoStock(savedEntity.getSaldo_actual())
                .build());
    }
}
