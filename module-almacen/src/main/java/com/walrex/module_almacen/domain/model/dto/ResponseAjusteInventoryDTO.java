package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAjusteInventoryDTO {
    private boolean isSuccess;
    private String message;
    private String transactionId;
    private ResultAjustIngresoDTO result_ingresos;
    private ResultAjustEgresoDTO result_egresos;
}
