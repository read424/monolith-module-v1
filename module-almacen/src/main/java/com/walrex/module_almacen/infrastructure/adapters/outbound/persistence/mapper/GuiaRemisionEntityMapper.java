package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionEntityMapper {
    /**
     * Actualiza una entidad existente con los datos de la guía de remisión
     * 
     * @param dto    Datos de la guía de remisión
     * @param entity Entidad existente a actualizar
     * @return Entidad actualizada
     */
    @Mapping(source = "idEmpresaTransp", target = "idEmpresaTransp")
    @Mapping(source = "idModalidad", target = "idModalidad")
    @Mapping(source = "idTipDocChofer", target = "idTipDocChofer")
    @Mapping(source = "numDocChofer", target = "numDocChofer")
    @Mapping(source = "numPlaca", target = "numPlaca")
    @Mapping(source = "idLlegada", target = "idLlegada")
    @Mapping(source = "idUsuario", target = "idUsuario")
    @Mapping(target = "id", ignore = true) // ✅
    @Mapping(target = "entregado", ignore = true) // ✅
    @Mapping(target = "status", ignore = true) // ✅
    @Mapping(target = "idMotivo", ignore = true) // ✅
    @Mapping(target = "idComprobante", ignore = true) // ✅
    @Mapping(target = "createAt", ignore = true) // ✅
    @Mapping(target = "updateAt", ignore = true) // ✅ No
    DevolucionServiciosEntity DTOtoEntity(
            GuiaRemisionGeneradaDTO dto);

    /**
     * Mapea entidad a DTO para respuesta
     */
    @InheritInverseConfiguration
    GuiaRemisionGeneradaDTO entityToDTO(DevolucionServiciosEntity entity);
}