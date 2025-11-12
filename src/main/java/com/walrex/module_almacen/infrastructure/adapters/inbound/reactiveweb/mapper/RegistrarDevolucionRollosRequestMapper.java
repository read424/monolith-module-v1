package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import java.math.BigDecimal;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.*;

/**
 * Mapper para convertir request de devolución a DTO de dominio usando MapStruct
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistrarDevolucionRollosRequestMapper {

    RegistrarDevolucionRollosRequestMapper INSTANCE = Mappers.getMapper(RegistrarDevolucionRollosRequestMapper.class);

    /**
     * Convierte request a DTO de dominio
     */
    @Mapping(target = "idOrdenSalida", ignore = true) // Se genera en el servicio
    @Mapping(target = "codSalida", ignore = true) // Se genera en el servicio
    @Mapping(target = "idUsuario", ignore = true) // Se asigna en el handler
    @Mapping(target = "idAlmacenOrigen", ignore = true) // Se asigna en el servicio
    @Mapping(target = "idAlmacenDestino", ignore = true) // Se asigna en el servicio
    @Mapping(source = "fechaDevolucion", target = "fechaRegistro")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "articulos", target = "articulos")
    SalidaDevolucionDTO requestToDto(RegistrarDevolucionRollosRequest request);

    /**
     * Convierte ArticuloDevolucionRequest a DevolucionArticuloDTO
     */
    @Mapping(target = "codArticulo", ignore = true) // Se asigna en el servicio
    @Mapping(target = "descArticulo", ignore = true) // Se asigna en el servicio
    @Mapping(target = "statusArticulo", ignore = true) // Se asigna en el servicio
    @Mapping(target = "idUnidad", ignore = true) // Se asigna en el servicio
    @Mapping(source = "totalPeso", target = "totalPeso", qualifiedByName = "convertDoubleToBigDecimal")
    DevolucionArticuloDTO mapArticuloRequest(ArticuloDevolucionRequest articuloRequest);

    /**
     * Convierte RolloDevolucionRequest a RolloDevolucionDTO
     */
    @Mapping(source = "idOrdenIngreso", target = "idRolloIngreso")
    @Mapping(source = "idDetOrdenIngreso", target = "idDetOrdenIngreso")
    @Mapping(source = "idDetOrdenIngresoPeso", target = "idDetOrdenIngresoPeso")
    @Mapping(source = "statusRolloIngreso", target = "statusRollIngreso")
    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "peso", target = "pesoRollo")
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idDetPartida", target = "idDetPartida")
    @Mapping(source = "sinCobro", target = "sinCobro")
    @Mapping(source = "statusRolloPartida", target = "statusRollPartida")
    @Mapping(source = "idOrdeningresoAlmacen", target = "idRolloIngresoAlmacen")
    @Mapping(source = "idDetordeningresoAlmacen", target = "idDetOrdenIngresoAlmacen")
    @Mapping(source = "idDetordeningresopesoAlmacen", target = "idDetOrdenIngresoPesoAlmacen")
    @Mapping(target = "statusRollIngresoPesoAlmacen", ignore = true) // Se asigna en el servicio
    @Mapping(target = "delete", ignore = true) // Campo no mapeado
    RolloDevolucionDTO mapRolloRequest(RolloDevolucionRequest rolloRequest);

    /**
     * Convierte String a Integer de forma segura
     */
    @Named("convertStringToInteger")
    default Integer convertStringToInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte Double a BigDecimal de forma segura
     */
    @Named("convertDoubleToBigDecimal")
    default BigDecimal convertDoubleToBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }
}