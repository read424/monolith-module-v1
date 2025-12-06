package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_partidas.domain.model.dto.ProcesoDeclararItemDTO;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.ProcesoIncompletoRequest;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.ProcesosPartidaIncompletosRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

/**
 * Mapper para convertir request de procesos incompletos a DTOs de dominio.
 * Utiliza MapStruct para generar el código de mapeo automáticamente.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcesoDeclararMapper {

    ProcesoDeclararMapper INSTANCE = Mappers.getMapper(ProcesoDeclararMapper.class);

    /**
     * Convierte ProcesoIncompletoRequest a ProcesoDeclararItemDTO
     *
     * @param request Request con datos del proceso incompleto
     * @return DTO de dominio con datos del proceso a declarar
     */
    ProcesoDeclararItemDTO toDTO(ProcesoIncompletoRequest request);

    /**
     * Convierte un array de ProcesoIncompletoRequest a una lista de ProcesoDeclararItemDTO
     *
     * @param requests Array de requests con procesos incompletos
     * @return Lista de DTOs de dominio
     */
    default List<ProcesoDeclararItemDTO> toDTOList(ProcesoIncompletoRequest[] requests) {
        if (requests == null) {
            return List.of();
        }
        return Arrays.stream(requests)
            .map(this::toDTO)
            .toList();
    }

    /**
     * Convierte ProcesosPartidaIncompletosRequest a lista de ProcesoDeclararItemDTO
     *
     * @param request Request completo con partida y procesos
     * @return Lista de DTOs de dominio
     */
    default List<ProcesoDeclararItemDTO> fromRequest(ProcesosPartidaIncompletosRequest request) {
        if (request == null || request.getProcesos() == null) {
            return List.of();
        }
        return toDTOList(request.getProcesos());
    }
}