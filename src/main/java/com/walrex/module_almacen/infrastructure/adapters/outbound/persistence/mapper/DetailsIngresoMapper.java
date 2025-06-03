package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetailsIngresoMapper {
    DetailsIngresoMapper INSTANCE = Mappers.getMapper(DetailsIngresoMapper.class);

    /**
     * Convierte un ItemProductDTO a DetailsIngresoEntity
     * Mapea los campos y calcula campos dependientes
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id_orden", target = "id_ordeningreso")
    @Mapping(source = "id_articulo", target = "id_articulo")
    @Mapping(source = "id_unidad", target = "id_unidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precio", target = "costo_compra")
    @Mapping(source = "observacion", target = "observacion")
    DetailsIngresoEntity toEntity(ItemProductDTO dto);

    /**
     * Convierte un DetailsIngresoEntity a ItemProductDTO
     * Mapea los campos y calcula campos adicionales como tot_amount
     */
    @Mapping(source = "id", target = "id_detail")
    @Mapping(source = "id_articulo", target = "id_articulo")
    @Mapping(source = "id_unidad", target = "id_unidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "costo_compra", target = "precio")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(target = "tot_amount", expression = "java(calcularTotalAmount(entity))")
    ItemProductDTO toDto(DetailsIngresoEntity entity);

    /**
     * Convierte una lista de ItemProductDTO a una lista de DetailsIngresoEntity
     */
    List<DetailsIngresoEntity> toEntityList(List<ItemProductDTO> dtoList);

    /**
     * Convierte una lista de DetailsIngresoEntity a una lista de ItemProductDTO
     */
    List<ItemProductDTO> toDtoList(List<DetailsIngresoEntity> entityList);

    /**
     * Calcula el monto total (precio * cantidad)
     */
    default Double calcularTotalAmount(DetailsIngresoEntity entity) {
        if (entity == null || entity.getCantidad() == null || entity.getCosto_compra() == null) {
            return 0.0;
        }
        return entity.getCantidad() * entity.getCosto_compra();
    }
}
