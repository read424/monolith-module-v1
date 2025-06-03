package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResultadoAjusteMapper {
    ResultadoAjusteMapper INSTANCE = Mappers.getMapper(ResultadoAjusteMapper.class);

    /**
     * Convierte un DetalleIngresoDTO a ItemResultSavedDTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "idArticulo", target = "id_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "costoCompra", target = "precio")
    ItemResultSavedDTO detalleIngresoToItemResult(DetalleIngresoDTO detalle);

    // Método para convertir una lista de DetalleIngresoDTO a lista de ItemResultSavedDTO
    List<ItemResultSavedDTO> detallesIngresoToItemResult(List<DetalleIngresoDTO> detalles);

    /**
     * Convierte un DetalleEgresoDTO a ItemResultSavedDTO
     */
    @Mapping(source = "id", target = "id_detalle_orden")
    @Mapping(source = "articulo.id", target = "id_articulo")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "idUnidad", target = "id_unidad")
    ItemArticuloEgreso detalleSalidaToItemResult(DetalleEgresoDTO detalle);

    // Método para convertir una lista de DetalleSalidaDTO a lista de ItemArticuloEgreso
    List<ItemArticuloEgreso> detallesSalidaToItemResult(List<DetalleEgresoDTO> detalles);

    /**
     * Construye un ResultAjustIngresoDTO a partir de una OrdenIngresoDTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(target = "num_saved", expression = "java(ordenIngreso.getDetalles() != null ? ordenIngreso.getDetalles().size() : 0)")
    @Mapping(target = "details", expression = "java(detallesIngresoToItemResult(ordenIngreso.getDetalles()))")
    ResultAjustIngresoDTO ordenIngresoToResultEvent(OrdenIngresoDTO ordenIngreso);

    /**
     * Construye un ResultAjustEgresoDTO a partir de una OrdenEgresoDTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(target = "num_saved", expression = "java(ordenEgreso.getDetalles() != null ? ordenEgreso.getDetalles().size() : 0)")
    @Mapping(target = "details", expression = "java(detallesSalidaToItemResult(ordenEgreso.getDetalles()))")
    ResultAjustEgresoDTO ordenEgresoToResultEvent(OrdenEgresoDTO ordenEgreso);
}
