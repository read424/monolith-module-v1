package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.DetalleRollo;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.ItemArticuloLogisticaRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.ItemRolloRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.OrdenIngresoLogisticaRequestDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenIngresoLogisticaMapper {
    OrdenIngresoLogisticaMapper INSTANCE = Mappers.getMapper(OrdenIngresoLogisticaMapper.class);

    @Mapping(source = "id_tipo_almacen.id_almacen", target = "almacen.idAlmacen")
    @Mapping(source = "motivo.id", target = "motivo.idMotivo")
    @Mapping(source = "motivo.descripcion", target = "motivo.descMotivo")
    @Mapping(source = "id_orden", target = "idOrdenCompra")
    @Mapping(source = "fec_ingreso", target = "fechaIngreso", qualifiedByName = "dateTimeToDate")
    @Mapping(source = "id_compro", target = "comprobante")
    @Mapping(source = "nu_serie", target = "codSerie")
    @Mapping(source = "nu_comprobante", target = "nroComprobante")
    @Mapping(source = "fec_ref", target = "fechaComprobante", qualifiedByName = "dateTimeToDate")
    @Mapping(source = "id_cliente", target = "idCliente")
    @Mapping(target = "detalles", source = "detalles", qualifiedByName = "itemsToDetalles")
    OrdenIngreso toOrdenIngreso(OrdenIngresoLogisticaRequestDto dto);

    // Mapeo de ItemArticuloLogisticaRequestDto a DetalleOrdenIngreso
    @Mapping(source = "idUnidadConsumo", target = "idUnidadSalida")
    @Mapping(source = "idDetOrdenCompra", target = "idDetalleOrdenCompra")
    @Mapping(source = "idArticulo", target = "articulo.id")
    @Mapping(source = "isMultiplo", target = "articulo.is_multiplo")
    @Mapping(source = "valorConversion", target = "articulo.valor_conv")
    @Mapping(source = "id_tipo_producto", target = "idTipoProducto")
    @Mapping(source = "id_tipo_producto_fa", target = "idTipoProductoFamilia")
    @Mapping(source = "exento_imp", target = "excentoImp")
    @Mapping(source = "mto_compra", target = "costo")
    @Mapping(source = "total", target = "montoTotal")
    @Mapping(target = "detallesRollos", ignore = true) // No hay datos de rollos en el DTO
    DetalleOrdenIngreso toDetalleOrdenIngreso(ItemArticuloLogisticaRequestDto detalle);

    // Mapeo de ItemRolloRequestDto a DetalleRollo
    @Mapping(source = "cod_rollo", target = "codRollo")
    @Mapping(source = "peso_rollo", target = "pesoRollo", qualifiedByName = "doubleToDecimal")
    @Mapping(target = "ordenIngreso", ignore = true) // Se completa después
    @Mapping(target = "idDetOrdenIngreso", ignore = true) // Se completa después
    DetalleRollo toDetalleRollo(ItemRolloRequestDto rolloDto);

    // Método para mapear listas (este método es opcional ya que MapStruct lo hace automáticamente)
    @Named("itemsToDetalles")
    default List<DetalleOrdenIngreso> itemsToDetalles(List<ItemArticuloLogisticaRequestDto> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toDetalleOrdenIngreso)
                .collect(Collectors.toList());
    }

    // Método para convertir arrays a listas (opcional)
    default List<DetalleRollo> toDetalleRollos(ItemRolloRequestDto[] rollos) {
        if (rollos == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(rollos)
                .map(this::toDetalleRollo)
                .collect(Collectors.toList());
    }

    // Método para convertir Double a BigDecimal
    @Named("doubleToDecimal")
    default BigDecimal doubleToDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    @Named("dateTimeToDate")
    default LocalDate dateTimeToDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }
}
