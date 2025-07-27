package com.walrex.module_ecomprobantes.domain.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferralGuideDTO {
    private String idVersion;
    private String typeGuide;
    private String codSerie;
    private Integer numCorrelativo;
    private String observacion;
    private LocalDate fecEmision;
    private CompanyDTO company;
    private ReceiverDTO receiver;
    private ShipmentDTO shipment;
    private List<DetailShipmentDTO> detalle;
}
