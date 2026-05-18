package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.ProductoEventoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoEventoPersistenceMapper {

    @Mapping(source = "id", target = "idProductoEvento")
    ProductoEventoEntity toEntity(ProductoEvento domain);

    @Mapping(source = "idProductoEvento", target = "id")
    ProductoEvento toDomain(ProductoEventoEntity entity);
}
