package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import java.time.LocalDate;

import org.mapstruct.*;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;
import com.walrex.module_ecomprobantes.domain.model.enums.TypeComprobante;

/**
 * Mapper para convertir CreateGuiaRemisionRemitenteMessage a ComprobanteDTO
 * 
 * CARACTERÍSTICAS:
 * - Usa MapStruct para mapeo automático
 * - Configurado como componente Spring
 * - Maneja conversiones específicas de tipos
 * - Aplica valores por defecto para campos requeridos
 * - Sigue principios de arquitectura hexagonal
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {
        TypeComprobante.class }, uses = { DetalleComprobantePersistenceMapper.class })
public interface GuiaRemisionComprobanteMapper {

    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idMotivo", target = "idMotivo")
    @Mapping(source = "tipoComprobante", target = "idTipoComprobante")
    @Mapping(source = "tipoSerie", target = "tipoSerie")
    @Mapping(source = "fechaEmision", target = "fechaEmision", qualifiedByName = "stringToLocalDate")
    @Mapping(source = "detailItems", target = "detalles") // ✅ Mapeo automático de detalles
    ComprobanteDTO toComprobanteDTO(CreateGuiaRemisionRemitenteMessage message);

    /**
     * Convierte String fecha a LocalDate
     * 
     * @param fechaString fecha en formato String (ISO)
     * @return LocalDate parseado
     */
    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String fechaString) {
        if (fechaString == null || fechaString.trim().isEmpty()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(fechaString);
        } catch (Exception e) {
            // Fallback a fecha actual si no se puede parsear
            return LocalDate.now();
        }
    }
}