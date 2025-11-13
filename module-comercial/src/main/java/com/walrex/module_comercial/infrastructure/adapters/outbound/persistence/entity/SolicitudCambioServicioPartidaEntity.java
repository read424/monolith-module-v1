package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia R2DBC para solicitudes de cambio de servicio de partidas.
 * Mapea a la tabla produccion.solicitudes_cambio_servicios_partidas.
 *
 * Esta entidad almacena tanto los valores antiguos (old) como los nuevos propuestos
 * para el cambio de servicio de una partida de producción.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table(name = "solicitudes_cambio_servicios_partidas", schema = "produccion")
public class SolicitudCambioServicioPartidaEntity {

    @Id
    @Column("id")
    private Integer id;

    // Datos de la partida afectada
    @Column("id_partida")
    private Integer idPartida;

    // Valores antiguos (OLD) - Estado actual antes del cambio
    @Column("id_ordenproduccion_old")
    private Integer idOrdenproduccionOld;

    @Column("id_orden_old")
    private Integer idOrdenOld;

    @Column("id_det_os_old")
    private Integer idDetOsOld;

    @Column("id_precio_old")
    private Integer idPrecioOld;

    @Column("precio_old")
    private Double precioOld;

    @Column("id_gama_old")
    private Integer idGamaOld;

    @Column("id_ruta_old")
    private Integer idRutaOld;

    @Column("desc_articulo_old")
    private String descArticuloOld;

    // Valores nuevos propuestos (pueden ser NULL si aún no se aprueban)
    @Column("id_ordenproduccion")
    private Integer idOrdenproduccion;

    @Column("id_orden")
    private Integer idOrden;

    @Column("id_det_os")
    private Integer idDetOs;

    @Column("id_ruta")
    private Integer idRuta;

    @Column("id_gama")
    private Integer idGama;

    @Column("id_precio")
    private Integer idPrecio;

    @Column("precio")
    private Double precio;

    // Estado y control de aprobación
    @Column("status")
    private Integer status;

    @Column("aprobado")
    private Integer aprobado;

    @Column("por_aprobar")
    private Integer porAprobar;

    @Column("partidas_adicionales")
    private Integer partidasAdicionales;

    // Datos de usuario
    @Column("id_usuario")
    private Integer idUsuario;

    @Column("id_usuario_autorizado")
    private Integer idUsuarioAutorizado;

    // Auditoría
    @Column("fec_registro")
    private LocalDate fecRegistro;

    @Column("create_at")
    private LocalDateTime createAt;

    @Column("update_at")
    private LocalDateTime updateAt;
}