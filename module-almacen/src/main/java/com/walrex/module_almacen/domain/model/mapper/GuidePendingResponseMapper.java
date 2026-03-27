package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.GuidePendingRecord;
import com.walrex.module_almacen.domain.model.dto.GuidePendingDetail;
import com.walrex.module_almacen.domain.model.dto.GuidePendingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuidePendingResponseMapper {

    GuidePendingDetail toDetail(GuidePendingRecord record);

    default GuidePendingResponse toResponse(GuidePendingRecord first, List<GuidePendingDetail> details) {
        return GuidePendingResponse.builder()
                .id_ordeningreso(first.getId_ordeningreso())
                .fec_registro(first.getFec_registro())
                .nu_serie(first.getNu_serie())
                .nu_comprobante(first.getNu_comprobante())
                .razon_social(first.getRazon_social())
                .details(details)
                .build();
    }
}
