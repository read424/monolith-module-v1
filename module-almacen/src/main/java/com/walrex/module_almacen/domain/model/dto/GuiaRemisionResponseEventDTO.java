package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuiaRemisionResponseEventDTO {
    private Boolean success;
    private String Message;
    private ResponseEventGuiaDataDto data;
}
