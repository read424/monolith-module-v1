package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper;

import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.ProcesoPartidaProjection;

/**
 * Mapper para convertir entre proyecciones y modelos de dominio de procesos de
 * partida
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcesoPartidaMapper {

    ProcesoPartidaMapper INSTANCE = Mappers.getMapper(ProcesoPartidaMapper.class);

    /**
     * Convierte ProcesoPartidaProjection a ProcesoPartida
     * 
     * @param projection Proyección del proceso de partida
     * @return Modelo de dominio del proceso de partida
     */
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idPartidaMaquina", target = "idPartidaMaquina")
    @Mapping(source = "idRuta", target = "idRuta")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "idProceso", target = "idProceso")
    @Mapping(source = "idDetRuta", target = "idDetRuta")
    @Mapping(source = "noProceso", target = "noProceso")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idMaquina", target = "idMaquina")
    @Mapping(source = "idTipoMaquina", target = "idTipoMaquina")
    @Mapping(source = "iniciado", target = "iniciado")
    @Mapping(source = "finalizado", target = "finalizado")
    @Mapping(source = "isPendiente", target = "isPendiente")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isMainProceso", target = "isMainProceso")
    @Mapping(source = "descMaq", target = "descMaq")
    ProcesoPartida toDomain(ProcesoPartidaProjection projection);

    /**
     * Convierte lista de ProcesoPartidaProjection a lista de ProcesoPartida
     * 
     * @param projections Lista de proyecciones de procesos de partida
     * @return Lista de modelos de dominio de procesos de partida
     */
    List<ProcesoPartida> toDomainList(List<ProcesoPartidaProjection> projections);
}
