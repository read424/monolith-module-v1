package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.KardexReporte;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KardexReporteMapper {

    default KardexReporte toKardexReporte(List<KardexArticuloDTO> articulos) {
        return KardexReporte.builder()
                .articulos(articulos)
                .totalArticulos(articulos.size())
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }
}
