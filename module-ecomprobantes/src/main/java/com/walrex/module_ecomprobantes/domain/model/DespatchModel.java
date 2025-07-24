package com.walrex.module_ecomprobantes.domain.model;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DespatchModel {
    private String numVersion;
    private String tipoDocumento;
    private String tipoSerie;
    private String nroCorrelativo;
    private LocalDate fecEmision;
    private CompanyModel company;
    private ClientModel destinatorio;
    private ShipmentModel envio;
    private List<DespatchDetailModel> detalle;
}
