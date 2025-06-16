package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarSalidaRequestDTO {
    @NotNull(message = "este campo es obligatorio")
    private Integer id_ordensalida;
    private String cod_salida;
    private Integer id_requerimiento;
    private Integer id_tipo_comprobante;
    private String desc_comprobante;
    @NotNull(message = "este campo es obligatorio")
    private Integer id_almacen_origen;
    @NotNull(message = "este campo es obligatorio")
    private Integer id_almacen_destino;
    private String cod_vale;
    private String no_motivo;
    @NotNull(message = "campo obligatorio")
    private Integer id_usuario_entrega;
    private String entregado;
    private Integer id_personal_supervisor;
    @NotNull(message = "campo obligatorio")
    private OffsetDateTime fec_entrega;
    private String cod_partida;
    @NotNull(message = "campo obligatorio")
    private List<ProductoSalidaDTO> productos;
}
