package com.walrex.module_ecomprobantes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DespatchDetailModel {
    private String descDetail;
    private String codDetail;
    private Double cntDetail;
    private String uniDetail;
}
