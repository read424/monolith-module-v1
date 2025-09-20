package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.*;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.*;

/**
 * Mapper para convertir entre DTOs de request y modelos de dominio de declinación de salida tacho
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeclineSalidaTachoMapper {

    DeclineSalidaTachoMapper INSTANCE = Mappers.getMapper(DeclineSalidaTachoMapper.class);

    /**
     * Convierte DeclineSalidaTacho (request) a DeclinePartidaTacho (domain)
     * 
     * @param request DTO de request de declinación de salida tacho
     * @return Modelo de dominio de partida tacho declinada
     */
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "lote", target = "lote")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "motivoRechazo", target = "motivoRechazo")
    @Mapping(source = "personal", target = "personal")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "rollos", target = "rollos")
    @Mapping(target = "idUsuario", ignore = true) // Se setea en el handler
    DeclinePartidaTacho toDomain(DeclineSalidaTachoRequest request);

    /**
     * Convierte DeclinePartidaTacho (domain) a DeclineSalidaTacho (request)
     * 
     * @param domain Modelo de dominio de partida tacho declinada
     * @return DTO de request de declinación de salida tacho
     */
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "lote", target = "lote")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "personal", target = "personal")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "rollos", target = "rollos")
    DeclineSalidaTachoRequest toRequest(DeclinePartidaTacho domain);

    /**
     * Convierte MotivoRechazo (request) a MotivoRechazoDomain (domain)
     * 
     * @param motivoRechazo Motivo de rechazo del request
     * @return Motivo de rechazo del dominio
     */
    @Mapping(source = "value", target = "value")
    @Mapping(source = "text", target = "text")
    MotivoRechazoDomain toMotivoRechazoDomain(MotivoRechazo motivoRechazo);

    /**
     * Convierte MotivoRechazoDomain (domain) a MotivoRechazo (request)
     * 
     * @param motivoRechazoDomain Motivo de rechazo del dominio
     * @return Motivo de rechazo del request
     */
    @Mapping(source = "value", target = "value")
    @Mapping(source = "text", target = "text")
    MotivoRechazo toMotivoRechazo(MotivoRechazoDomain motivoRechazoDomain);

    /**
     * Convierte PersonalSupervisor (request) a PersonalSupervisorDomain (domain)
     * 
     * @param personalSupervisor Personal supervisor del request
     * @return Personal supervisor del dominio
     */
    @Mapping(source = "idPersonal", target = "idPersonal")
    @Mapping(source = "apenomEmpleado", target = "apenomEmpleado")
    PersonalSupervisorDomain toPersonalSupervisorDomain(PersonalSupervisor personalSupervisor);

    /**
     * Convierte PersonalSupervisorDomain (domain) a PersonalSupervisor (request)
     * 
     * @param personalSupervisorDomain Personal supervisor del dominio
     * @return Personal supervisor del request
     */
    @Mapping(source = "idPersonal", target = "idPersonal")
    @Mapping(source = "apenomEmpleado", target = "apenomEmpleado")
    PersonalSupervisor toPersonalSupervisor(PersonalSupervisorDomain personalSupervisorDomain);

    /**
     * Mapeo personalizado de List<RolloTacho> a List<ItemRolloProcess>
     * 
     * @param rollos Lista de rollos del request
     * @return Lista de items de rollo del dominio
     */
    List<ItemRolloProcess> toItemRollos(List<RolloTacho> rollos);

    /**
     * Mapeo personalizado de List<ItemRolloProcess> a List<RolloTacho>
     * 
     * @param itemRollos Lista de items de rollo del dominio
     * @return Lista de rollos del request
     */
    List<RolloTacho> toRollos(List<ItemRolloProcess> itemRollos);

    /**
     * Convierte RolloTacho (request) a ItemRolloProcess (domain)
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
    ItemRolloProcess toItemRolloProcess(RolloTacho rolloTacho);

    /**
     * Convierte ItemRolloProcess (domain) a RolloTacho (request)
     * 
     * @param itemRolloProcess Item de rollo del dominio
     * @return Rollo del request
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
    RolloTacho toRolloTacho(ItemRolloProcess itemRolloProcess);
}
