package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.user.module_users.domain.model.PasswordRecoveryToken;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.PasswordRecoveryTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasswordRecoveryTokenMapper {
    PasswordRecoveryTokenMapper INSTANCE = Mappers.getMapper(PasswordRecoveryTokenMapper.class);

    @Mapping(target = "status", expression = "java(mapStatus(token.getStatus()))")
    PasswordRecoveryTokenEntity domainToEntity(PasswordRecoveryToken token);

    @Mapping(target = "status", expression = "java(mapTokenStatus(entity.getStatus()))")
    PasswordRecoveryToken entityToDomain(PasswordRecoveryTokenEntity entity);

    default Integer mapStatus(PasswordRecoveryToken.TokenStatus status) {
        if (status == null) return 0;
        switch (status) {
            case PENDING: return 0;
            case USED: return 1;
            case EXPIRED: return 2;
            default: return 0;
        }
    }

    default PasswordRecoveryToken.TokenStatus mapTokenStatus(Integer status) {
        if (status == null) return PasswordRecoveryToken.TokenStatus.PENDING;
        switch (status) {
            case 0: return PasswordRecoveryToken.TokenStatus.PENDING;
            case 1: return PasswordRecoveryToken.TokenStatus.USED;
            case 2: return PasswordRecoveryToken.TokenStatus.EXPIRED;
            default: return PasswordRecoveryToken.TokenStatus.PENDING;
        }
    }
}
