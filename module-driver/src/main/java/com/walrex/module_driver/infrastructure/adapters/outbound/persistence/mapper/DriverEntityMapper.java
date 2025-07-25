package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.mapper;

import java.time.*;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.DriverEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface DriverEntityMapper {

    @Mapping(source = "idDriver", target = "idConductor")
    @Mapping(source = "idUsuario", target = "idUser")
    @Mapping(source = "idTipoDocumento", target = "idTipoDoc")
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "status", constant = "1")
    DriverEntity toEntity(CreateDriverDTO dto);

    @Mapping(source = "idConductor", target = "idDriver")
    @Mapping(source = "idTipoDoc", target = "idTipoDocumento")
    @Mapping(source = "idUser", target = "idUsuario")
    CreateDriverDTO toDTO(DriverEntity entity);

    @Named("mapLocalDateTimeToOffsetTime")
    default OffsetTime mapLocalDateTimeToOffsetTime(LocalDateTime localDateTime) {
        return OffsetTime.of(localDateTime.toLocalTime(), ZoneOffset.UTC);
    }
}
