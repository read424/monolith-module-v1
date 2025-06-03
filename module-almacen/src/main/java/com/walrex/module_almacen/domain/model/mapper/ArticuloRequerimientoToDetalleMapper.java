package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.dto.ArticuloRequerimiento;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.ProductoAprobadoDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticuloRequerimientoToDetalleMapper {
    @Mapping(target = "id", source = "idDetalleOrden", qualifiedByName = "integerToLong")
    @Mapping(target = "articulo.id", source = "idArticulo")
    @Mapping(target = "articulo.descripcion", source = "descArticulo")
    @Mapping(target = "idUnidad", source = "idUnidad")
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "a_lotes", ignore = true)
    DetalleEgresoDTO toDetalleEgreso(ArticuloRequerimiento articulo);

    // ✅ Mapeo para ProductoAprobadoDTO
    @Mapping(target = "idDetalleOrden", source = "idDetalleOrden")
    @Mapping(target = "idArticulo", source = "idArticulo")
    @Mapping(target = "descripcionArticulo", source = "descArticulo")
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "unidad", source = "abrevUnidad")
    @Mapping(target = "status", source = "selected")
    @Mapping(target = "motivo", constant = "Producto seleccionado para salida")
    ProductoAprobadoDTO toProductoAprobado(ArticuloRequerimiento articulo);

    @Named("integerToLong")
    default Long integerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }
}
