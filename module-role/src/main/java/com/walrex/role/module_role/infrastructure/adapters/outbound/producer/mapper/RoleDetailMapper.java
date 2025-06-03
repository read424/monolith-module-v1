package com.walrex.role.module_role.infrastructure.adapters.outbound.producer.mapper;

import com.walrex.avro.schemas.RoleResponseMessage;
import com.walrex.avro.schemas.WinPermissionMessage;
import com.walrex.role.module_role.domain.model.RolDetailDTO;
import com.walrex.role.module_role.domain.model.RolDetailItemDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Collections.class)
public interface RoleDetailMapper {
    RoleDetailMapper INSTANCE = Mappers.getMapper(RoleDetailMapper.class);

    @Mapping(source = "name_rol", target = "noRol")
    @Mapping(source = "details", target = "details", qualifiedByName = "detailsToWinPermissionMessages")
    RoleResponseMessage rolDetailDtoToAvro(RolDetailDTO dto);

    // Mapeo de RolDetailItemDTO a WinPermissionMessage
    @Mapping(source = "idwin_state", target = "idwinState")
    @Mapping(source = "name_state", target = "nameState")
    @Mapping(source = "id_parent_win", target = "idParentWin")
    @Mapping(source = "type_state", target = "typeState")
    WinPermissionMessage rolDetailItemDtoToWinPermissionMessage(RolDetailItemDTO dto);

    @Named("detailsToWinPermissionMessages")
    default List<WinPermissionMessage> mapDetailToPermisoDto(List<RolDetailItemDTO> details) {
        if (details == null) {
            return Collections.emptyList();
        }
        return details.stream()
                .map(this::rolDetailItemDtoToWinPermissionMessage)
                .collect(Collectors.toList());
    }
}
