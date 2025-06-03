package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Motivo;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.ItemArticuloTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.enums.TypeAlmacen;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenSalidaTransformacionMapper {

    @Mapping(source = "fec_ingreso", target = "fecRegistro")
    @Mapping(source = "fec_ingreso", target = "fecEntrega")
    @Mapping(source = ".", target = "motivo", qualifiedByName = "assignedMotivo" )
    @Mapping(source = ".", target = "almacenOrigen", qualifiedByName = "assignedAlmacenOrigen")
    @Mapping(source = ".", target = "almacenDestino", qualifiedByName = "assignedAlmacenDestino")
    @Mapping(source = "detalles", target = "detalles") // ✅ Mapear detalles
    @Mapping(target = "entregado", constant = "0") // ✅ Inicialmente no entregado
    @Mapping(target = "status", constant = "1") // ✅ Estado activo
    OrdenEgresoDTO toOrdenEgreso(OrdenIngresoTransformacionDTO dto);

    @Mapping(source = "id_articulo", target = "articulo.id")
    @Mapping(source = "cod_articulo", target = "articulo.codigo")
    @Mapping(source = "desc_articulo", target = "articulo.descripcion")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(target = "entregado", constant = "0") // ✅ Inicialmente no entregado
    @Mapping(target = "status", constant = "1") // ✅ Estado activo
    DetalleEgresoDTO toDetalleEgreso(ItemArticuloTransformacionDTO dto);

    @Named("assignedAlmacenOrigen")
    default Almacen assignedAlmacenOrigen(OrdenIngresoTransformacionDTO dto){
        return Almacen.builder()
                .idAlmacen(TypeAlmacen.INSUMOS.getId())
                .build();
    }

    @Named("assignedAlmacenDestino")
    default Almacen assignedAlmacenDestino(OrdenIngresoTransformacionDTO dto){
        return Almacen.builder()
                .idAlmacen(TypeAlmacen.INSUMOS.getId())
        .build();
    }

    @Named("assignedMotivo")
    default Motivo assignedMotivo(OrdenIngresoTransformacionDTO dto) {
        return Motivo.builder()
                .idMotivo(20)
                .build();
    }

    @Named("assignedIsInterno")
    default Integer assignedIsInterno(){
        return TypeMovimiento.INTERNO_TRANSFORMACION.getId();
    }
}
