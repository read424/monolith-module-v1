package com.walrex.user.module_users.domain.mapper;

import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import com.walrex.user.module_users.infrastructure.adapters.security.dto.UserDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Collections.class)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id_employee", target = "id_empleado")
    @Mapping(source = "username", target = "no_usuario")
    @Mapping(target = "state_default", ignore = true)
    @Mapping(target = "state_menu", expression = "java(Collections.emptyMap())")
    @Mapping(target = "permissions", expression = "java(Collections.emptyList())")
    UserDetailDTO toUserDetailDTO(UserEntity userEntity);
}
