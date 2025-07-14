package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import org.mapstruct.*;

import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.ComprobanteEntity;

/**
 * Mapper para convertir ComprobanteDTO a ComprobanteEntity
 * 
 * CARACTERÍSTICAS:
 * - Usa MapStruct para mapeo automático
 * - Configurado como componente Spring
 * - Maneja valores por defecto para campos requeridos
 * - Aplica fecha actual para nuevos registros
 * - Incluye mapeo de detalles usando DetalleComprobanteDTOMapper
 * - Sigue principios de arquitectura hexagonal
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        DetalleComprobanteDTOMapper.class })
public interface ComprobantePersistenceMapper {

    /**
     * Convierte ComprobanteDTO a ComprobanteEntity para persistencia
     * 
     * @param dto ComprobanteDTO del dominio
     * @return ComprobanteEntity mapeado para R2DBC
     */
    @Mapping(source = "idComprobante", target = "idComprobante")
    @Mapping(source = "idTipoComprobante", target = "idTipoComprobante")
    @Mapping(source = "tipoSerie", target = "tipoSerie")
    @Mapping(source = "numeroComprobante", target = "numeroComprobante")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "fechaEmision", target = "fechaEmision")
    @Mapping(source = "fechaVencimiento", target = "fechaVencimiento")
    @Mapping(source = "idTipoMoneda", target = "idTipoMoneda", qualifiedByName = "defaultTipoMoneda")
    @Mapping(source = "idPago", target = "idPago")
    @Mapping(source = "idFormaPago", target = "idFormaPago")
    @Mapping(source = "idTipoRetencion", target = "idTipoRetencion")
    @Mapping(source = "subtotal", target = "subtotal")
    @Mapping(source = "igv", target = "igv")
    @Mapping(source = "total", target = "total")
    @Mapping(source = "idMotivo", target = "idMotivo")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "idModalidad", target = "idModalidad")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "codigoResponseSunat", target = "codigoResponseSunat")
    @Mapping(source = "responseSunat", target = "responseSunat")
    @Mapping(source = "fechaComunicacion", target = "fechaComunicacion")
    @Mapping(source = "descripcionMotivo", target = "descripcionMotivo")
    @Mapping(source = "aplicaDetraccion", target = "aplicaDetraccion")
    @Mapping(source = "isInafecta", target = "isInafecta")
    @Mapping(source = "isDocumentoAutorizado", target = "isDocumentoAutorizado")
    @Mapping(source = "notesSunat", target = "notesSunat")

    // ✅ Campos automáticos
    @Mapping(expression = "java(java.time.LocalDate.now())", target = "fechaRegistro")
    @Mapping(constant = "0", target = "anulado")

    // ✅ Campos auditoria se manejan automáticamente por R2DBC
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)

    ComprobanteEntity toEntity(ComprobanteDTO dto);

    /**
     * Convierte ComprobanteEntity a ComprobanteDTO para respuesta
     * 
     * @param entity ComprobanteEntity desde la base de datos
     * @return ComprobanteDTO del dominio
     */
    @Mapping(target = "fechaRegistro", source = "fechaRegistro")

    // ✅ Los detalles se cargan por separado usando el servicio
    @Mapping(target = "detalles", ignore = true)

    ComprobanteDTO toDTO(ComprobanteEntity entity);

    /**
     * Aplica valor por defecto para tipo de moneda si es null
     * 
     * @param idTipoMoneda ID del tipo de moneda
     * @return ID del tipo de moneda o 1 (PEN) por defecto
     */
    @Named("defaultTipoMoneda")
    default Integer defaultTipoMoneda(Integer idTipoMoneda) {
        return idTipoMoneda != null ? idTipoMoneda : 1; // PEN por defecto
    }
}