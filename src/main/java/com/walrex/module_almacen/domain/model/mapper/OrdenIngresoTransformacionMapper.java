package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.enums.TypeAlmacen;
import com.walrex.module_almacen.domain.model.enums.TypeCurrency;
import com.walrex.module_almacen.domain.model.enums.TypeUnidadMedida;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenIngresoTransformacionMapper {

    @Mapping(source = "fec_ingreso", target = "fechaIngreso")
    @Mapping(source = "almacen", target = "almacen")
    @Mapping(source = ".", target = "detalles", qualifiedByName = "crearDetalleUnico")
    OrdenIngreso toOrdenIngreso(OrdenIngresoTransformacionDTO dto);

    @Named("crearDetalleUnico")
    default List<DetalleOrdenIngreso> crearDetalleUnico(OrdenIngresoTransformacionDTO dto) {
        if (dto == null || dto.getArticulo().getIdArticulo() == null) {
            return Collections.emptyList();
        }

        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(dto.getArticulo().getIdArticulo())
                        .build())
                .idUnidad(TypeUnidadMedida.KILO.getId())
                .cantidad(BigDecimal.valueOf(dto.getCantidad()))
                .idMoneda(TypeCurrency.DOLLAR.getId())
                .costo(BigDecimal.valueOf(dto.getPrecio()))
                .build();

        return List.of(detalle);
    }

    // Mapeo directo para almacén con valor por defecto
    default Almacen mapAlmacen(Almacen almacen) {
        if (almacen != null && almacen.getIdAlmacen() != null) {
            return almacen;
        }
        return Almacen.builder()
                .idAlmacen(TypeAlmacen.INSUMOS.getId())
                .build();
    }
}