package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import java.math.BigDecimal;

import org.mapstruct.*;
import org.springframework.stereotype.Component;

import com.walrex.module_almacen.domain.model.dto.DetailItemGuiaRemisionDTO;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DevolucionServicioEntityMapper {

    @Mapping(source = "id", target = "idDevolucion")
    @Mapping(target = "idCliente", ignore = true)
    GuiaRemisionGeneradaDTO toDto(DevolucionServiciosEntity devolucionServicios);

    @Mapping(source = "id_detalle_orden", target = "idDetalleOrden")
    @Mapping(source = "id_ordensalida", target = "idOrdenSalida")
    @Mapping(source = "id_articulo", target = "idProducto")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "precio", target = "precio", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "tot_kilos", target = "peso", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "tot_monto", target = "total", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "idTipoServicio", ignore = true)
    DetailItemGuiaRemisionDTO toDetailItemGuiaRemisionDTO(DetailSalidaEntity detailSalida);

    /**
     * Convierte Double a BigDecimal de forma segura
     */
    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }
}
