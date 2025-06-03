package com.walrex.user.module_users.infrastructure.adapters.inbound.consumer.mapper;

import com.walrex.avro.schemas.RoleResponseMessage;
import com.walrex.avro.schemas.WinPermissionMessage;
import com.walrex.user.module_users.domain.model.RolDetailDTO;
import com.walrex.user.module_users.domain.model.RolDetailItemDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Collections.class)
public interface UserRoleDetailMapper {
    UserRoleDetailMapper INSTANCE = Mappers.getMapper(UserRoleDetailMapper.class);

    @Mapping(source = "noRol", target = "name_rol")
    @Mapping(source = "details", target = "details", qualifiedByName = "winPermissionMessagesToDetails")
    RolDetailDTO avroToRolDetailDto(RoleResponseMessage avro);

    @Mapping(source = "idwinState", target = "idwin_state")
    @Mapping(source = "nameState", target = "name_state")
    @Mapping(source = "idParentWin", target = "id_parent_win")
    @Mapping(source = "typeState", target = "type_state")
    RolDetailItemDTO winPermissionMessageToRolDetailItemDto(WinPermissionMessage avro);

    @Named("winPermissionMessagesToDetails")
    default List<RolDetailItemDTO> mapWinPermissionMessagesToDetails(List<WinPermissionMessage> permissions) {
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(this::winPermissionMessageToRolDetailItemDto)
                .collect(Collectors.toList());
    }
}
