package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_laboratorio.domain.model.CurvaDisenoItem;
import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.projection.RecetaProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RecetaProjectionMapper {

    @Mapping(source = "projection.idReceta",  target = "id")
    @Mapping(source = "projection.codReceta", target = "codReceta")
    @Mapping(source = "projection.razonSocial", target = "razonSocial")
    @Mapping(source = "projection.codColores",  target = "codColores")
    @Mapping(source = "projection.noColores",   target = "noColores")
    @Mapping(source = "projection.status",      target = "status")
    @Mapping(source = "projection.compartir",   target = "compartir", qualifiedByName = "parseCompartir")
    @Mapping(source = "projection.noGama",      target = "noGama")
    @Mapping(source = "projection.noColor",     target = "noColor")
    @Mapping(source = "projection.noTenido",    target = "noTenido")
    @Mapping(source = "curvas",                 target = "curvaDiseno")
    public abstract Receta toDomain(RecetaProjection projection, List<CurvaDisenoItem> curvas);

    @Named("parseCompartir")
    protected Boolean parseCompartir(String value) {
        if (value == null) return false;
        return switch (value.trim().toUpperCase()) {
            case "S", "1", "T", "Y", "TRUE" -> true;
            default -> false;
        };
    }
}
