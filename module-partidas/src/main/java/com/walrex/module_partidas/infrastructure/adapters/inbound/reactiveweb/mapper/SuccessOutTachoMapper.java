package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.ItemRolloResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.SucessOutTachoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SuccessOutTachoMapper {
    
    SuccessOutTachoMapper INSTANCE = Mappers.getMapper(SuccessOutTachoMapper.class);

    @Mapping(source = "idOrdeningreso", target = "idOrdenIngreso")
    @Mapping(source = "codIngreso", target = "codIngreso")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    SucessOutTachoResponse toSuccessOutTachoResponse(IngresoAlmacenDTO ingresoAlmacen);

    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "idIngresopeso", target = "idDetordeningresopeso")
    @Mapping(source = "idRolloIngreso", target = "idRolloIngreso")
    @Mapping(source= "pesoRollo", target = "pesoRollo")
    ItemRolloResponse toItemRolloResponse(ItemRollo itemRollo);

}
