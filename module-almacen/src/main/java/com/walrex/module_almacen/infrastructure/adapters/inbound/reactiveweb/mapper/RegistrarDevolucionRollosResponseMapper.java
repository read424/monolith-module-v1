package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_almacen.domain.model.dto.SalidaDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.RegistrarDevolucionRollosResponse;

/**
 * 🔄 Mapper para transformar SalidaDevolucionDTO a
 * RegistrarDevolucionRollosResponse
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegistrarDevolucionRollosResponseMapper {

    RegistrarDevolucionRollosResponseMapper INSTANCE = Mappers.getMapper(RegistrarDevolucionRollosResponseMapper.class);

    /**
     * 🔄 Mapea SalidaDevolucionDTO a RegistrarDevolucionRollosResponse
     * 
     * @param devolucionDTO DTO de dominio con la información de devolución
     * @return Response simplificado con código, total kg y total rollos
     */
    @Mapping(source = "codSalida", target = "codSalida")
    @Mapping(source = ".", target = "totalKg", qualifiedByName = "calcularTotalKg")
    @Mapping(source = ".", target = "totalRollos", qualifiedByName = "calcularTotalRollos")
    RegistrarDevolucionRollosResponse toResponse(SalidaDevolucionDTO devolucionDTO);

    /**
     * 🧮 Calcula el total de kg procesados sumando todos los rollos
     */
    @Named("calcularTotalKg")
    default Double calcularTotalKg(SalidaDevolucionDTO devolucionDTO) {
        return devolucionDTO.getArticulos().stream()
                .flatMap(articulo -> articulo.getRollos().stream())
                .mapToDouble(rollo -> rollo.getPesoRollo().doubleValue())
                .sum();
    }

    /**
     * 🔢 Calcula el total de rollos procesados contando todos los rollos
     */
    @Named("calcularTotalRollos")
    default Integer calcularTotalRollos(SalidaDevolucionDTO devolucionDTO) {
        return devolucionDTO.getArticulos().stream()
                .mapToInt(articulo -> articulo.getRollos().size())
                .sum();
    }
}