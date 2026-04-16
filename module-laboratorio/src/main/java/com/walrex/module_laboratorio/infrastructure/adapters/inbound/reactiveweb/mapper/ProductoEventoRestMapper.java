package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_laboratorio.domain.model.PagedResponse;
import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.ProductoEventoCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.ProductoEventoUpdateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.PaginatedResponse;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.ProductoEventoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductoEventoRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    ProductoEvento toDomain(ProductoEventoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    ProductoEvento toDomain(ProductoEventoUpdateRequest request);

    ProductoEventoResponse toResponse(ProductoEvento domain);

    default PaginatedResponse<ProductoEventoResponse> toPaginatedResponse(PagedResponse<ProductoEvento> paged) {
        return PaginatedResponse.<ProductoEventoResponse>builder()
                .content(paged.content().stream().map(this::toResponse).toList())
                .pagination(PaginatedResponse.PageMetadata.builder()
                        .page(Math.max(paged.page() - 1, 0))
                        .size(paged.size())
                        .totalElements(paged.totalElements())
                        .totalPages(paged.totalPages())
                        .numberOfElements(paged.content().size())
                        .first(paged.first())
                        .last(paged.last())
                        .hasPrevious(paged.page() > 1)
                        .hasNext(paged.hasMore())
                        .build())
                .build();
    }
}
