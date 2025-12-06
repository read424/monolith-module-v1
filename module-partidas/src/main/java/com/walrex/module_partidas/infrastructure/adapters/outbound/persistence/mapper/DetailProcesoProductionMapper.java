package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_partidas.domain.model.dto.DetailProcesoProductionDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.DetailProcesoPartidaProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir entre proyección de proceso de partida y DTO de detalle de proceso de producción.
 * Utiliza MapStruct para generar el código de mapeo automáticamente.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetailProcesoProductionMapper {

    DetailProcesoProductionMapper INSTANCE = Mappers.getMapper(DetailProcesoProductionMapper.class);

    /**
     * Convierte DetailProcesoPartidaProjection a DetailProcesoProductionDTO
     *
     * @param projection Proyección del proceso de partida
     * @return DTO de detalle de proceso de producción
     */
    @Mapping(source = "id_partida_maquina", target = "idPartidaMaquina")
    @Mapping(source = "id_proceso", target = "idProceso")
    @Mapping(source = "id_det_ruta", target = "idDetRuta")
    @Mapping(source = "id_tipo_maquina", target = "idTipoMaquina")
    @Mapping(source = "id_maquina", target = "idMaquina")
    @Mapping(source = "isservicio", target = "isServicio")
    @Mapping(source = "fec_real_inicio", target = "fecRealInicio")
    @Mapping(source = "hora_inicio", target = "horaInicio")
    @Mapping(source = "fec_real_fin", target = "fecRealFin")
    @Mapping(source = "hora_fin", target = "horaFin")
    @Mapping(source = "id_nivel_observ", target = "idNivelObservacion")
    @Mapping(source = "is_main_proceso", target = "isMainProceso")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "observacion", target = "observacion")
    DetailProcesoProductionDTO toDTO(DetailProcesoPartidaProjection projection);
}