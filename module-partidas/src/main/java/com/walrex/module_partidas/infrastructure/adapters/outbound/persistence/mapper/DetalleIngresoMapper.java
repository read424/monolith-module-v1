package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper;

import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.DetalleIngresoRollos;
import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.DetalleIngresoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.ItemRolloProjection;

/**
 * Mapper para convertir entre proyecciones y modelos de dominio del detalle de
 * ingreso
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleIngresoMapper {

    DetalleIngresoMapper INSTANCE = Mappers.getMapper(DetalleIngresoMapper.class);

    /**
     * Convierte DetalleIngresoProjection a DetalleIngresoRollos
     * 
     * @param projection Proyección del detalle de ingreso
     * @return Modelo de dominio del detalle de ingreso
     */
    @Mapping(target = "rollos", ignore = true) // Se asigna manualmente en el adapter
    @Mapping(target = "idDetordeningreso", ignore = true) // Se construye como lista en el adapter
    @Mapping(target = "idOrdeningreso", ignore = true) // Se construye como lista en el adapter
    DetalleIngresoRollos toDomain(DetalleIngresoProjection projection);

    /**
     * Convierte ItemRolloProjection a ItemRollo
     * 
     * @param projection Proyección del rollo
     * @return Modelo de dominio del rollo
     */
    ItemRollo toDomain(ItemRolloProjection projection);

    /**
     * Convierte lista de ItemRolloProjection a lista de ItemRollo
     * 
     * @param projections Lista de proyecciones de rollos
     * @return Lista de modelos de dominio de rollos
     */
    List<ItemRollo> toDomainList(List<ItemRolloProjection> projections);
}
