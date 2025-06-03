package com.walrex.module_articulos.infrastructure.adapters.inbound.consumer.mapper;

import com.walrex.avro.schemas.GetCodesArticulosEvents;
import com.walrex.avro.schemas.ListCodigosArticulos;
import com.walrex.avro.schemas.GetCodesArticulosEvents;
import com.walrex.avro.schemas.ListCodigosArticulos;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvroMapper {
    AvroMapper INSTANCE = Mappers.getMapper(AvroMapper.class);

    default List<String> mapToCodigos(GetCodesArticulosEvents event) {
        if (event == null || event.getCodigosArticulos() == null) {
            return Collections.emptyList();
        }
        return event.getCodigosArticulos().stream()
                .map(ListCodigosArticulos::getCodArticulo)
                .collect(Collectors.toList());
    }
}
