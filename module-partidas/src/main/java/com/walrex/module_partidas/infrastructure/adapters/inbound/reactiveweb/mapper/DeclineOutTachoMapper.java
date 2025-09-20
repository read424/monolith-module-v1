package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.DeclineOutTachoResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.ItemRolloResponse;

/**
 * Mapper para convertir entre DTOs de respuesta de declinación de salida tacho
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeclineOutTachoMapper {
    
    DeclineOutTachoMapper INSTANCE = Mappers.getMapper(DeclineOutTachoMapper.class);

    /**
     * Convierte IngresoAlmacenDTO (domain) a DeclineOutTachoResponse (response)
     * 
     * @param ingresoAlmacenDTO DTO de ingreso al almacén
     * @return DTO de respuesta de declinación
     */
    @Mapping(source = "idOrdeningreso", target = "idOrdenIngreso")
    @Mapping(source = "codIngreso", target = "codIngreso")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "cntRollos", target = "cntRollos")
    @Mapping(source = "pesoRef", target = "pesoRef")
    @Mapping(source = "rollos", target = "rollos")
    @Mapping(source = "ingresos", target = "ingresos")
    @Mapping(source = "cntRollosAlmacen", target = "cntRollosAlmacen")
    @Mapping(target = "motivoRechazo", ignore = true) // Se setea manualmente
    @Mapping(target = "personalSupervisor", ignore = true) // Se setea manualmente
    @Mapping(target = "observacion", ignore = true) // Se setea manualmente
    DeclineOutTachoResponse toDeclineOutTachoResponse(IngresoAlmacenDTO ingresoAlmacenDTO);

    /**
     * Convierte ItemRollo (domain) a ItemRolloResponse (response)
     * 
     * @param itemRollo Item de rollo del dominio
     * @return Item de rollo de respuesta
     */
    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "idIngresopeso", target = "idDetordeningresopeso")
    @Mapping(source = "idRolloIngreso", target = "idRolloIngreso")
    @Mapping(source = "pesoRollo", target = "pesoRollo")
    ItemRolloResponse toItemRolloResponse(ItemRollo itemRollo);
}
