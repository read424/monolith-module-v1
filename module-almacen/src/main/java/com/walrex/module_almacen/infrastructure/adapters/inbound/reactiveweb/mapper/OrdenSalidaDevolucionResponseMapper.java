package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.OrdenSalidaDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper para convertir OrdenSalidaDevolucionDTO a OrdenSalidaDevolucionResponse.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 * MapStruct generará automáticamente métodos para listas basándose en el método individual.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenSalidaDevolucionResponseMapper {
    
    /**
     * Convierte un OrdenSalidaDevolucionDTO a OrdenSalidaDevolucionResponse.
     * 
     * @param dto el DTO de dominio
     * @return el response para la API
     */
    OrdenSalidaDevolucionResponse toResponse(OrdenSalidaDevolucionDTO dto);

} 