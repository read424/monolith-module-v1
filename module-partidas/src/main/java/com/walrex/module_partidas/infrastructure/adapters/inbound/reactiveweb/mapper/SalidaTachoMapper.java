package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.ItemRolloProcess;
import com.walrex.module_partidas.domain.model.SuccessPartidaTacho;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.RolloTacho;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.SavedSalidaTacho;

/**
 * Mapper para convertir entre DTOs de request y modelos de dominio de salida tacho
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalidaTachoMapper {

    SalidaTachoMapper INSTANCE = Mappers.getMapper(SalidaTachoMapper.class);

    /**
     * Convierte SavedSalidaTacho (request) a SuccessPartidaTacho (domain)
     * 
     * @param request DTO de request de salida tacho
     * @return Modelo de dominio de partida tacho exitosa
     */
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "lote", target = "lote")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "idSupervisor", target = "idSupervisor")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "rollos", target = "rollos")
    SuccessPartidaTacho toDomain(SavedSalidaTacho request);

    /**
     * Convierte SuccessPartidaTacho (domain) a SavedSalidaTacho (request)
     * 
     * @param domain Modelo de dominio de partida tacho exitosa
     * @return DTO de request de salida tacho
     */
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "lote", target = "lote")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "idSupervisor", target = "idSupervisor")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "rollos", target = "rollos")
    SavedSalidaTacho toRequest(SuccessPartidaTacho domain);

    /**
     * Mapeo personalizado de List<RolloTacho> a List<ItemRollo>
     * 
     * @param rollos Lista de rollos del request
     * @return Lista de items de rollo del dominio
     */
    List<ItemRolloProcess> toItemRollos(List<RolloTacho> rollos);

    /**
     * Mapeo personalizado de List<ItemRollo> a List<RolloTacho>
     * 
     * @param itemRollos Lista de items de rollo del dominio
     * @return Lista de rollos del request
     */
    List<RolloTacho> toRollos(List<ItemRolloProcess> itemRollos);

    /**
     * Convierte RolloTacho (request) a ItemRollo (domain)
     * 
     * @param rolloTacho Rollo del request
     * @return Item de rollo del dominio
     */
    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "pesoRollo", target = "pesoRollo")
    @Mapping(source = "idOrdenIngreso", target = "idOrdenIngreso")
    @Mapping(source = "idIngresoPeso", target = "idIngresoPeso")
    @Mapping(source = "idIngresoAlmacen", target = "idIngresoAlmacen")
    @Mapping(source = "idRolloIngreso", target = "idRolloIngreso")
    @Mapping(source = "idDetPartida", target = "idDetPartida")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "selected", target = "selected")
    ItemRolloProcess toItemRollo(RolloTacho rolloTacho);

    /**
     * Convierte ItemRollo (domain) a RolloTacho (request)
     * 
     * @param itemRollo Item de rollo del dominio
     * @return Rollo del request
     */
    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "pesoRollo", target = "pesoRollo")
    @Mapping(source = "idOrdenIngreso", target = "idOrdenIngreso")
    @Mapping(source = "idIngresoPeso", target = "idIngresoPeso")
    @Mapping(source = "idIngresoAlmacen", target = "idIngresoAlmacen")
    @Mapping(source = "idRolloIngreso", target = "idRolloIngreso")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "selected", target = "selected")
    RolloTacho toRolloTacho(ItemRolloProcess itemRollo);
}