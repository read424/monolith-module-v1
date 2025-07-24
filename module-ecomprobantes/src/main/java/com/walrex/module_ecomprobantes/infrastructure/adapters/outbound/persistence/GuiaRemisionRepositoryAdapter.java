package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionDataPort;
import com.walrex.module_ecomprobantes.domain.model.*;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionDataDTO;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para guías de remisión.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuiaRemisionRepositoryAdapter implements GuiaRemisionDataPort {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<GuiaRemisionDataDTO> obtenerDatosGuiaRemision(Integer idComprobante) {
        log.info("🔍 Obteniendo datos de guía de remisión para comprobante: {}", idComprobante);

        String query = buildQueryComprobante();

        return databaseClient.sql(query)
                .bind("idComprobante", idComprobante)
                .map((row, metadata) -> mapRowToGuiaRemisionData(row))
                .one()
                .doOnNext(data -> log.info("✅ Datos obtenidos para comprobante: {}", idComprobante))
                .doOnError(error -> log.error("❌ Error obteniendo datos: {}", error.getMessage()));
    }

    /**
     * Construye la query para obtener datos de guía de remisión por ID de
     * comprobante.
     */
    private String buildQueryComprobante() {
        return """
                SELECT
                    c.id_comprobante,
                    c.fecha_emision,
                    c.fecha_entrega,
                    c.observaciones,
                    c.id_motivo_traslado,
                    mt.descripcion as desc_motivo_traslado,
                    c.id_modalidad,
                    m.descripcion as desc_modalidad,
                    c.id_empresa_trans,
                    et.razon_social as desc_empresa_trans,
                    c.id_conductor,
                    c.num_placa,
                    c.id_llegada,
                    l.descripcion as desc_llegada,
                    c.is_guia_sunat,
                    c.numero_guia,
                    c.serie_guia,
                    c.correlativo_guia,

                    -- Datos del cliente
                    cli.razon_social as cliente_razon_social,
                    cli.numero_documento as cliente_numero_documento,
                    cli.tipo_documento as cliente_tipo_documento,

                    -- Datos de la empresa
                    emp.razon_social as empresa_razon_social,
                    emp.ruc as empresa_ruc,
                    emp.direccion as empresa_direccion,

                    -- Datos del transportista
                    car.razon_social as carrier_razon_social,
                    car.numero_documento as carrier_numero_documento,
                    car.tipo_documento as carrier_tipo_documento,

                    -- Datos del conductor
                    d.nombres as driver_nombres,
                    d.apellidos as driver_apellidos,
                    d.numero_documento as driver_numero_documento,
                    d.tipo_documento as driver_tipo_documento,
                    d.numero_licencia as driver_numero_licencia,

                    -- Datos del vehículo
                    v.placa as vehiculo_placa,
                    v.marca as vehiculo_marca,
                    v.modelo as vehiculo_modelo,
                    v.anio as vehiculo_anio,

                    -- Datos del envío
                    s.peso_total as shipment_peso_total,
                    s.volumen_total as shipment_volumen_total,
                    s.cantidad_bultos as shipment_cantidad_bultos,

                    -- Datos del despacho
                    desp.numero_despacho,
                    desp.fecha_despacho,
                    desp.estado as despacho_estado

                FROM ecomprobantes.comprobante c
                LEFT JOIN ecomprobantes.motivo_traslado mt ON mt.id_motivo_traslado = c.id_motivo_traslado
                LEFT JOIN ecomprobantes.modalidad m ON m.id_modalidad = c.id_modalidad
                LEFT JOIN ecomprobantes.empresa_transporte et ON et.id_empresa_trans = c.id_empresa_trans
                LEFT JOIN ecomprobantes.llegada l ON l.id_llegada = c.id_llegada
                LEFT JOIN ventas.cliente cli ON cli.id_cliente = c.id_cliente
                LEFT JOIN ventas.empresa emp ON emp.id_empresa = c.id_empresa
                LEFT JOIN ventas.carrier car ON car.id_carrier = c.id_carrier
                LEFT JOIN ventas.conductor d ON d.id_conductor = c.id_conductor
                LEFT JOIN ventas.vehiculo v ON v.placa = c.num_placa
                LEFT JOIN ecomprobantes.shipment s ON s.id_comprobante = c.id_comprobante
                LEFT JOIN ecomprobantes.despacho desp ON desp.id_comprobante = c.id_comprobante
                WHERE c.id_comprobante = :idComprobante
                """;
    }

    /**
     * Mapea una fila de la base de datos a GuiaRemisionDataDTO.
     */
    private GuiaRemisionDataDTO mapRowToGuiaRemisionData(Row row) {
        return GuiaRemisionDataDTO.builder()
                .idOrdenSalida(row.get("id_comprobante", Integer.class))
                .fechaEmision(row.get("fecha_emision", LocalDate.class))
                .fechaEntrega(row.get("fecha_entrega", LocalDate.class))
                .observaciones(row.get("observaciones", String.class))
                .idMotivoTraslado(row.get("id_motivo_traslado", Integer.class))
                .descMotivoTraslado(row.get("desc_motivo_traslado", String.class))
                .idModalidad(row.get("id_modalidad", Integer.class))
                .descModalidad(row.get("desc_modalidad", String.class))
                .idEmpresaTrans(row.get("id_empresa_trans", Integer.class))
                .descEmpresaTrans(row.get("desc_empresa_trans", String.class))
                .idConductor(row.get("id_conductor", Integer.class))
                .numPlaca(row.get("num_placa", String.class))
                .idLlegada(row.get("id_llegada", Integer.class))
                .descLlegada(row.get("desc_llegada", String.class))
                .isGuiaSunat(row.get("is_guia_sunat", Boolean.class))
                .numeroGuia(row.get("numero_guia", String.class))
                .serieGuia(row.get("serie_guia", String.class))
                .correlativoGuia(row.get("correlativo_guia", String.class))

                // Mapear entidades
                .client(mapRowToClientModel(row))
                .company(mapRowToCompanyModel(row))
                .carrier(mapRowToCarrierModel(row))
                .driver(mapRowToDriverPersonModel(row))
                .vehicle(mapRowToVehicleModel(row))
                .shipment(mapRowToShipmentModel(row))
                .despatch(mapRowToDespatchModel(row))

                .build();
    }

    /**
     * Mapea los datos del cliente.
     */
    private ClientModel mapRowToClientModel(Row row) {
        return ClientModel.builder()
                .tipDocument(row.get("cliente_tipo_documento", String.class))
                .numDocument(row.get("cliente_numero_documento", String.class))
                .razonSocial(row.get("cliente_razon_social", String.class))
                .build();
    }

    /**
     * Mapea los datos de la empresa.
     */
    private CompanyModel mapRowToCompanyModel(Row row) {
        return CompanyModel.builder()
                .numRuc(row.get("empresa_ruc", String.class))
                .razonSocial(row.get("empresa_razon_social", String.class))
                .build();
    }

    /**
     * Mapea los datos del transportista.
     */
    private CarrierModel mapRowToCarrierModel(Row row) {
        return CarrierModel.builder()
                .tipoDocumento(row.get("carrier_tipo_documento", String.class))
                .numDocumento(row.get("carrier_numero_documento", String.class))
                .razonSocial(row.get("carrier_razon_social", String.class))
                .nroMTC(null) // Valor por defecto
                .build();
    }

    /**
     * Mapea los datos del conductor.
     */
    private DriverPersonModel mapRowToDriverPersonModel(Row row) {
        return DriverPersonModel.builder()
                .idTipoDocumento(1) // Valor por defecto
                .numDocumento(row.get("driver_numero_documento", String.class))
                .nombres(row.get("driver_nombres", String.class))
                .apellidos(row.get("driver_apellidos", String.class))
                .numLicencia(row.get("driver_numero_licencia", String.class))
                .build();
    }

    /**
     * Mapea los datos del vehículo.
     */
    private VehicleModel mapRowToVehicleModel(Row row) {
        return VehicleModel.builder()
                .numPlaca(row.get("vehiculo_placa", String.class))
                .vehicleSeconds(new ArrayList<>()) // Lista vacía por defecto
                .build();
    }

    /**
     * Mapea los datos del envío.
     */
    private ShipmentModel mapRowToShipmentModel(Row row) {
        return ShipmentModel.builder()
                .codTraslado("01") // Valor por defecto
                .modTraslado("01") // Valor por defecto
                .fechaTraslado(LocalDate.now()) // Valor por defecto
                .pesoTotal(row.get("shipment_peso_total", Double.class))
                .unidadPeso("KGM") // Valor por defecto
                .llegada(null) // Se puede enriquecer después
                .partida(null) // Se puede enriquecer después
                .transportista(null) // Se puede enriquecer después
                .build();
    }

    /**
     * Mapea los datos del despacho.
     */
    private DespatchModel mapRowToDespatchModel(Row row) {
        return DespatchModel.builder()
                .numVersion("2.1") // Valor por defecto
                .tipoDocumento("09") // Guía de remisión
                .tipoSerie("T001") // Valor por defecto
                .nroCorrelativo(row.get("numero_despacho", String.class))
                .fecEmision(row.get("fecha_despacho", LocalDate.class))
                .company(null) // Se puede enriquecer después
                .destinatorio(null) // Se puede enriquecer después
                .envio(null) // Se puede enriquecer después
                .detalle(new ArrayList<>()) // Lista vacía por defecto
                .build();
    }
}