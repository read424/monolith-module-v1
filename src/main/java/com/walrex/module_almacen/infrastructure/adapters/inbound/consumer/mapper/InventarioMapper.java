package com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.mapper;

import com.walrex.avro.schemas.AjustInventaryMessage;
import com.walrex.avro.schemas.ItemArticuloMessage;
import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.domain.model.dto.RequestAjusteInventoryDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventarioMapper {
    InventarioMapper INSTANCE = Mappers.getMapper(InventarioMapper.class);

    @Mapping(source = "idMotivo", target = "id_motivo")
    @Mapping(source = "idAlmacen", target = "id_almacen")
    @Mapping(source = "fecCreacion", target = "fec_actualizacion", qualifiedByName = "stringToLocalDate")
    @Mapping(source = "ingresos", target = "ingresos")
    @Mapping(source = "egresos", target = "egresos")
    RequestAjusteInventoryDTO mapAvroToDto(AjustInventaryMessage message);

    @Mapping(target = "tot_amount", expression = "java(calcularTotalAmount(item))")
    ItemProductDTO mapItemToDto(ItemArticuloMessage item);

    List<ItemProductDTO> mapItemsToDto(List<ItemArticuloMessage> items);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    @Named("calcularTotalAmount")
    default Double calcularTotalAmount(ItemArticuloMessage item) {
        if(item ==null){
            return 0.00;
        }
        try{
            float cantidad = item.getCantidad();
            float precio = item.getPrecio();
            if (Float.isNaN(cantidad) || Float.isNaN(precio)
                    || Float.isInfinite(cantidad) || Float.isInfinite(precio)
            ) {
                return 0.0;
            }
            return (double) cantidad*precio;
        }catch(Exception e){
            return 0.0;
        }
    }
}
