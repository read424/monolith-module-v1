package com.walrex.user.module_users.infrastructure.adapters.outbound.producer.mapper;

import com.walrex.avro.schemas.DataTemplateRecoveryEmail;
import com.walrex.avro.schemas.PasswordRecoveryEvent;
import com.walrex.user.module_users.domain.model.PasswordRecoveryData;
import com.walrex.user.module_users.domain.model.UserDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasswordRecoveryMapper {
    PasswordRecoveryMapper INSTANCE = Mappers.getMapper(PasswordRecoveryMapper.class);

    @Mapping(target = "email", source = "recoveryData.email")
    @Mapping(target = "subject", constant = "Recuperación de contraseña")
    @Mapping(target = "templateHtml", source = "template")
    @Mapping(target = "data", source = "recoveryData", qualifiedByName = "createTemplateData")
    PasswordRecoveryEvent toPasswordRecoveryEvent(PasswordRecoveryData recoveryData, String template);

    @Named("createTemplateData")
    default DataTemplateRecoveryEmail createTemplateData(PasswordRecoveryData recoveryData) {
        return DataTemplateRecoveryEmail.newBuilder()
                .setUser(recoveryData.getUsername())
                .setCodigo(recoveryData.getRecoveryCode())
                .setLinkRecovery("https://tudominio.com/resetPassword?token=" +
                        recoveryData.getRecoveryCode())
                .build();
    }
}
