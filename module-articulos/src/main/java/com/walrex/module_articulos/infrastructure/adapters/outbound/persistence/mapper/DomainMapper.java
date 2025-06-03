package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_articulos.domain.model.Articulo;
import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DomainMapper {
    DomainMapper INSTANCE = Mappers.getMapper(DomainMapper.class);

    @Mapping(source = "id", target = "idArticulo")
    @Mapping(source = "id_familia", target = "idFamilia")
    @Mapping(source = "id_grupo", target = "idGrupo")
    @Mapping(source = "cod_articulo", target = "codArticulo")
    @Mapping(source = "desc_articulo", target = "descArticulo")
    @Mapping(source = "id_medida", target = "idMedida")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "id_marca", target = "idMarca")
    @Mapping(source = "mto_compra", target = "mtoCompra")
    @Mapping(source = "create_at", target = "fecIngreso")
    @Mapping(source = "id_unidad_consumo", target = "idUnidadConsumo")
    @Mapping(source = "id_moneda", target = "idMoneda")
    @Mapping(source = "is_transformacion", target = "isTransformacion")
    Articulo entityToDomain(ArticuloEntity entity);

    @InheritInverseConfiguration
    ArticuloEntity domainToEntity(Articulo domain);

    /**
     * Convierte OffsetDateTime a LocalDateTime con formato YYYY-MM-DD
     */
    default LocalDateTime map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        // Convertir a LocalDateTime, descartando la información de zona horaria
        return offsetDateTime.toLocalDateTime();
        // Nota: Si solo quieres la fecha sin tiempo (YYYY-MM-DD), podrías usar:
        // return offsetDateTime.toLocalDate().atStartOfDay();
    }

    /**
     * Mapeo inverso de LocalDateTime a LocalDate para el campo create_at
     */
    default LocalDate map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toLocalDate();
    }
}
