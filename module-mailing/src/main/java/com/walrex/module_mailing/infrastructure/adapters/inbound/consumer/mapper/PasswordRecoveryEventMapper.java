package com.walrex.module_mailing.infrastructure.adapters.inbound.consumer.mapper;

import com.walrex.avro.schemas.PasswordRecoveryEvent;
import com.walrex.avro.schemas.DataTemplateRecoveryEmail;
import com.walrex.module_mailing.domain.model.MailMessage;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasswordRecoveryEventMapper {
    PasswordRecoveryEventMapper INSTANCE = Mappers.getMapper(PasswordRecoveryEventMapper.class);

    @Mapping(source = "email", target = "to")
    @Mapping(source = "subject", target = "subject")
    @Mapping(source = "templateHtml", target = "template")
    @Mapping(source = "data", target = "variables", qualifiedByName = "mapDataToVariables")
    MailMessage toMailMessage(PasswordRecoveryEvent event);

    @Named("mapDataToVariables")
    static Map<String, Object> mapDataToVariables(DataTemplateRecoveryEmail data) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", data.getUser());
        variables.put("codigo", data.getCodigo());
        variables.put("link_recovery", data.getLinkRecovery());
        return variables;
    }
}
