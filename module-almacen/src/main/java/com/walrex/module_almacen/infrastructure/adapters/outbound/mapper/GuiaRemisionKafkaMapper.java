package com.walrex.module_almacen.infrastructure.adapters.outbound.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionDataProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionItemProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.GuiaRemisionKafkaRepository;

import reactor.core.publisher.Mono;

@Component
public class GuiaRemisionKafkaMapper {

    @Autowired
    private GuiaRemisionKafkaRepository guiaRemisionKafkaRepository;

    /**
     * Convierte datos de orden de salida a GuiaRemisionRemitenteMessage
     * incluyendo los items de la orden de salida
     */
    public Mono<CreateGuiaRemisionRemitenteMessage> toGuiaRemisionRemitenteMessage(Long idOrdenSalida) {
        return Mono.zip(
                guiaRemisionKafkaRepository.findGuiaRemisionData(idOrdenSalida),
                guiaRemisionKafkaRepository.findGuiaRemisionItems(idOrdenSalida)
                        .map(this::toItemGuiaRemisionRemitenteMessage)
                        .collectList())
                .map(tuple -> {
                    var guiaData = tuple.getT1();
                    var items = tuple.getT2();
                    return mapearGuiaRemisionMessage(guiaData, items);
                });
    }

    /**
     * Mapea los datos de la guía de remisión con sus items
     * NOTA: Usando esquema temporal hasta que se genere
     * CreateGuiaRemisionRemitenteMessage
     */
    private CreateGuiaRemisionRemitenteMessage mapearGuiaRemisionMessage(
            GuiaRemisionDataProjection guiaData,
            List<ItemGuiaRemisionRemitenteMessage> items) {

        // TODO: Cambiar a CreateGuiaRemisionRemitenteMessage cuando esté disponible
        return CreateGuiaRemisionRemitenteMessage.newBuilder()
                .setIdCliente(guiaData.getIdCliente())
                .setIdMotivo(guiaData.getIdMotivo())
                .setFechaEmision(guiaData.getFechaEmision() != null ? guiaData.getFechaEmision().toString() : "")
                .setDetailItems(items)
                .build();
    }

    /**
     * Convierte los datos de item a ItemGuiaRemisionRemitenteMessage
     */
    public ItemGuiaRemisionRemitenteMessage toItemGuiaRemisionRemitenteMessage(
            GuiaRemisionItemProjection item) {

        return ItemGuiaRemisionRemitenteMessage.newBuilder()
                .setIdProducto(item.getIdProducto())
                .setIdOrdensalida(item.getIdOrdensalida())
                .setCantidad(item.getCantidad())
                .setPrecio(item.getPrecio())
                .setSubtotal(item.getSubtotal())
                .setIdDetalleOrden(item.getIdDetalleOrden())
                .setPeso(item.getPeso())
                .setIdUnidad(item.getIdUnidad())
                .setTipoServicio(item.getTipoServicio())
                .build();
    }
}