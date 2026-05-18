package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GamaResponse {
    private Integer id;
    private String name;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Short order;
}
