package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import org.mapstruct.*;

import com.walrex.module_ecomprobantes.domain.model.dto.DetalleComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.DetalleComprobanteEntity;

/**
 * Mapper bidireccional para DetalleComprobanteDTO ↔ DetalleComprobanteEntity
 * 
 * CARACTERÍSTICAS:
 * - Usa MapStruct para mapeo automático
 * - Configurado como componente Spring
 * - Mapeo directo entre campos equivalentes
 * - Soporte para listas de detalles
 * - Sigue principios de arquitectura hexagonal
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleComprobanteDTOMapper {

    /**
     * Convierte DetalleComprobanteDTO a DetalleComprobanteEntity
     * 
     * @param dto DetalleComprobanteDTO del dominio
     * @return DetalleComprobanteEntity para persistencia
     */
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    DetalleComprobanteEntity toEntity(DetalleComprobanteDTO dto);

    /**
     * Convierte DetalleComprobanteEntity a DetalleComprobanteDTO
     * 
     * @param entity DetalleComprobanteEntity desde la base de datos
     * @return DetalleComprobanteDTO del dominio
     */
    DetalleComprobanteDTO toDTO(DetalleComprobanteEntity entity);
}