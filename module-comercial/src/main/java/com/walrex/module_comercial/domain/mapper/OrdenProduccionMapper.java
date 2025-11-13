package com.walrex.module_comercial.domain.mapper;

import com.walrex.module_comercial.domain.dto.OrdenProduccionRequestDTO;
import com.walrex.module_comercial.domain.dto.OrdenProduccionResponseDTO;
import com.walrex.module_comercial.domain.model.OrdenProduccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper MapStruct para convertir entre DTOs y entidades de dominio de OrdenProduccion.
 * Se genera automáticamente en tiempo de compilación.
 *
 * Responsabilidad: Traducir DTOs al modelo de dominio y viceversa.
 * Pertenece a la capa de dominio (domain layer).
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenProduccionMapper {

    OrdenProduccionMapper INSTANCE = Mappers.getMapper(OrdenProduccionMapper.class);

    /**
     * Convierte un DTO de request a entidad de dominio.
     * Los campos de auditoría (createAt, updateAt) y el ID se ignoran
     * ya que se establecen en la capa de persistencia.
     *
     * @param dto DTO de request
     * @return Modelo de dominio OrdenProduccion
     */
    @Mapping(target = "idOrdenProduccion", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "fecRegistro", ignore = true)
    @Mapping(target = "status", ignore = true)
    OrdenProduccion toModel(OrdenProduccionRequestDTO dto);

    /**
     * Convierte una entidad de dominio a DTO de respuesta (Java Record).
     * MapStruct maneja automáticamente la conversión a records.
     *
     * @param model Modelo de dominio OrdenProduccion
     * @return DTO de respuesta (record)
     */
    OrdenProduccionResponseDTO toResponseDTO(OrdenProduccion model);

    /**
     * Convierte un DTO de respuesta (record) a entidad de dominio.
     * Útil para operaciones de actualización o reconstrucción de modelo.
     *
     * @param dto DTO de respuesta (record)
     * @return Modelo de dominio OrdenProduccion
     */
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    OrdenProduccion toModelFromResponse(OrdenProduccionResponseDTO dto);
}