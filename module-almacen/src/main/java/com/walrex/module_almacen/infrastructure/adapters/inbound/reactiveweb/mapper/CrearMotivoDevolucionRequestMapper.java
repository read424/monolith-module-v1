package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.CrearMotivoDevolucionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir CrearMotivoDevolucionRequest a MotivoDevolucionDTO
 * Siguiendo el patrón de separación entre request de API y DTO de dominio
 */
@Mapper(componentModel = "spring")
public interface CrearMotivoDevolucionRequestMapper {

    CrearMotivoDevolucionRequestMapper INSTANCE = Mappers.getMapper(CrearMotivoDevolucionRequestMapper.class);

    /**
     * Convierte un request de creación a DTO de dominio
     * Aplica valores por defecto y excluye campos que serán asignados por el sistema
     * 
     * @param request Request de la API con datos de entrada
     * @return DTO del dominio listo para procesamiento
     */
    @Mapping(target = "id", ignore = true)                          // Será asignado por la BD
    @Mapping(target = "descripcion", source = "descripcion")        // Mapeo directo
    @Mapping(target = "status", constant = "1")                     // Activo por defecto
    @Mapping(target = "createAt", ignore = true)                    // Será asignado por la BD
    @Mapping(target = "updateAt", ignore = true)                    // Será asignado por la BD
    MotivoDevolucionDTO toDTO(CrearMotivoDevolucionRequest request);
} 