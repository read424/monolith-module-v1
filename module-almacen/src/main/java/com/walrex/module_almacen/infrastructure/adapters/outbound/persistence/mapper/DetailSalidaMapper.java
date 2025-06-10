package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.DetalleSalidaDTO;
import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetailSalidaMapper {

    @Mapping(source = "id_ordensalida", target = "idOrdenEgreso")
    @Mapping(source = "id_detalle_orden", target = "id")
    @Mapping(source = "tot_monto", target = "totalMonto")
    DetalleEgresoDTO toDto(DetailSalidaEntity entity);

    @Mapping(source = "idOrdenEgreso", target = "id_ordensalida")
    @Mapping(source = "id", target = "id_detalle_orden")
    @Mapping(source = "totalMonto", target = "tot_monto")
    @Mapping(source = "articulo.id", target = "id_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    DetailSalidaEntity toEntity(DetalleEgresoDTO dto);

    List<DetailSalidaEntity> toEntityList(List<ItemProductDTO> dtos);
    List<ItemProductDTO> toDtoList(List<DetailSalidaEntity> entities);

    @Mapping(source = "id_detalle_orden", target = "id")
    @Mapping(source = "id_ordensalida", target = "idOrdenSalida")
    @Mapping(source = "id_articulo", target = "idArticulo")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precio", target = "precio")
    @Mapping(source = "tot_monto", target = "totalMonto")
    DetalleSalidaDTO toSalidaDto(DetailSalidaEntity entity);

    @AfterMapping
    default void mapLongValues(@MappingTarget DetailSalidaEntity entity, ItemProductDTO dto) {
        if (dto.getId_orden() != null) {
            entity.setId_ordensalida(dto.getId_orden());
        }
        if (dto.getId_detail() != null) {
            entity.setId_ordensalida(dto.getId_detail());
        }
    }

    @AfterMapping
    default void mapIntegerValues(@MappingTarget ItemProductDTO dto, DetailSalidaEntity entity) {
        if (entity.getId_ordensalida() != null) {
            dto.setId_orden(entity.getId_ordensalida().longValue());
        }
        if (entity.getId_ordensalida() != null) {
            dto.setId_detail(Long.valueOf(entity.getId_ordensalida()));
        }
    }

    @AfterMapping
    default void mapIntegerValuesToDTO(@MappingTarget DetalleSalidaDTO dto, DetailSalidaEntity entity) {
        if (entity.getId_ordensalida() != null) {
            dto.setId(Long.valueOf(entity.getId_ordensalida()));
        }
        if (entity.getId_ordensalida() != null) {
            dto.setIdOrdenSalida(Long.valueOf(entity.getId_ordensalida()));
        }
    }
}
