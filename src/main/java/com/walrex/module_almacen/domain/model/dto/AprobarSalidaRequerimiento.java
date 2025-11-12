package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarSalidaRequerimiento {
    @JsonProperty("id_ordensalida")
    private Integer idOrdenSalida;
    @JsonProperty("cod_salida")
    private String codOrdenSalida;
    @JsonProperty("id_requerimiento")
    private Integer idRequerimiento;
    @JsonProperty("id_tipo_requerimiento")
    private Integer idTipoComprobante;
    @JsonProperty("id_almacen_origen")
    private Integer idAlmacenOrigen;
    @JsonProperty("id_almacen_destino")
    private Integer idAlmacenDestino;
    private Integer idUsuario;
    @JsonProperty("id_usuario_entrega")
    private Integer idUsuarioEntrega;
    private String entregado;
    @JsonProperty("id_personal_supervisor")
    private Integer idUsuarioSupervisor;
    private Integer idUsuarioDeclara;
    @JsonProperty("fec_entrega")
    private Date fecEntrega;
    @JsonProperty("productos")
    private List<ArticuloRequerimiento> detalles;
}
