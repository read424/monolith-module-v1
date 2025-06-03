package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.user.module_users.domain.model.UserDto;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEntityMapper {
    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    UserDto entityToDto(UserEntity entity);
    UserEntity dtoToEntity(UserDto dto);
}
