package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticuloIngresoLogisticaMapper {
    ArticuloIngresoLogisticaMapper INSTANCE = Mappers.getMapper(ArticuloIngresoLogisticaMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "integerToLong")
    @Mapping(source = "articulo.id", target = "id_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "cantidad", target = "cantidad", qualifiedByName = "bigDecimalToDouble")
    @Mapping(source = "costo", target = "costo_compra", qualifiedByName = "bigDecimalToDouble")
    @Mapping(source = "excentoImp", target = "excento_imp", qualifiedByName = "booleanToInt")
    @Mapping(source = "idMoneda", target = "id_moneda")
    DetailsIngresoEntity toEntity(DetalleOrdenIngreso dto);

    // Método para convertir una lista de DetalleOrdenIngreso a lista de DetailsIngresoEntity
    List<DetailsIngresoEntity> detallesIngresoToItemEntity(List<DetalleOrdenIngreso> detalles);

    @Named("integerToLong")
    default Long integerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.doubleValue() : null;
    }

    @Named("booleanToInt")
    default Integer booleanToInt(Boolean excentoImp) {
        return Boolean.TRUE.equals(excentoImp) ? 1 : 0;
    }
}
