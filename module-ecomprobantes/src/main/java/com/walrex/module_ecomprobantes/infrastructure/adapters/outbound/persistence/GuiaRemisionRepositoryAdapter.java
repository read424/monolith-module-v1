package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionDataPort;
import com.walrex.module_ecomprobantes.domain.model.*;
import com.walrex.module_ecomprobantes.domain.model.dto.*;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
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

    @Override
    public Mono<GuiaRemisionCabeceraProjection> obtenerProyeccionGuiaRemision(Integer idComprobante) {
        log.info("🔍 Obteniendo proyección de guía de remisión para comprobante: {}", idComprobante);

        return obtenerCabeceraGuiaRemision(idComprobante)
                .flatMap(cabecera -> obtenerDetallesGuiaRemision(idComprobante)
                        .collectList()
                        .map(detalles -> {
                            cabecera.setDetalles(detalles);
                            return cabecera;
                        }))
                .doOnNext(data -> log.info("✅ Proyección obtenida para comprobante: {}", idComprobante))
                .doOnError(error -> log.error("❌ Error obteniendo proyección: {}", error.getMessage()));
    }

    /**
     * Obtiene la cabecera de la guía de remisión.
     */
    private Mono<GuiaRemisionCabeceraProjection> obtenerCabeceraGuiaRemision(Integer idComprobante) {
        String query = buildQueryCabeceraGuiaRemision();

        return databaseClient.sql(query)
                .bind("idComprobante", idComprobante)
                .map((row, metadata) -> mapRowToCabeceraProjection(row))
                .one();
    }

    /**
     * Obtiene los detalles de la guía de remisión.
     */
    private Flux<GuiaRemisionDetalleProjection> obtenerDetallesGuiaRemision(Integer idComprobante) {
        String query = buildQueryDetallesGuiaRemision();

        return databaseClient.sql(query)
                .bind("idComprobante", idComprobante)
                .map((row, metadata) -> mapRowToDetalleProjection(row))
                .all();
    }

    /**
     * Construye la query para obtener la cabecera de guía de remisión.
     */
    private String buildQueryCabeceraGuiaRemision() {
        return """
                SELECT c.id_comprobante, doc_compr.nu_compro, ts.nu_serie, c.nro_comprobante, c.fe_emision, company.num_documento
                , company.razon_social AS company_razon_social, sucursal.direccion AS company_direccion, sucursal.tlf_principal, sucursal.num_fax
                , sucursal.tlf_secundario
                , doc_client.cod_tipodoc, doc_client.abrev_doc, client.nu_ruc
                , CASE WHEN client.id_tipodoc=3 THEN client.no_razon ELSE CONCAT_WS(', ', TRIM(client.no_apepat)||' '||TRIM(client.no_apemat), TRIM(client.no_nombres )) END AS rzn_social, client.no_dir
                , mt.cod_motivo_traslado, mt.desc_motivo_traslado, mod_tras.cod_mod_traslado, mod_tras.desc_mod_traslado
                , doc_transp.cod_tipodoc AS tip_doc_transp, doc_transp.abrev_doc AS abrev_doc_transp
                , transp.num_tipo_documento, transp.razon_social
                , llegada.direccion AS direc_llegada, dist_lleg.ubigeo AS ubigeo_llegada
                , sucursal.direccion AS direc_partida, dist_comp.ubigeo AS ubigeo_partida
                , ds.num_placa
                , doc_conductor.cod_tipodoc AS cod_tipodoc_conductor, doc_conductor.no_tipodoc AS tipo_doc_conductor
                , doc_conductor.abrev_doc AS abrev_doc_conductor
                , conductor.num_documento AS num_documento_conductor, conductor.nombres AS nombres_conductor
                , conductor.apellidos AS apellidos_conductor, conductor.num_licencia AS num_licencia_conductor
                FROM facturacion.tbcomprobantes c
                LEFT OUTER JOIN facturacion.tipo_serie ts ON ts.id_serie = c.tctipo_serie
                LEFT OUTER JOIN facturacion.comprobantes doc_compr ON doc_compr.id_compro = ts.id_compro
                LEFT OUTER JOIN comercial.tbclientes AS client ON client.id_cliente = c.id_cliente
                LEFT OUTER JOIN rrhh.tctipo_doc AS doc_client ON doc_client.id_tipodoc = client.id_tipodoc
                LEFT OUTER JOIN almacenes.devolucion_servicios ds ON ds.id_comprobante = c.id_comprobante
                LEFT OUTER JOIN ventas.motivo_traslado mt ON mt.id_motivo_traslado = ds.motivo_comprobante
                LEFT OUTER JOIN ventas.modalidad_traslado mod_tras ON mod_tras.id_mod_traslado = ds.id_modalidad
                LEFT OUTER JOIN ventas.tb_transportista transp ON transp.id_transportista = ds.id_empresa_transp
                LEFT OUTER JOIN ventas.tb_conductor conductor ON conductor.id_conductor = ds.id_conductor
                LEFT OUTER JOIN rrhh.tctipo_doc AS doc_conductor ON doc_conductor.id_tipodoc = conductor.id_tipo_doc
                LEFT OUTER JOIN rrhh.tctipo_doc AS doc_transp ON doc_transp.id_tipodoc = transp.id_tipo_documento
                LEFT OUTER JOIN comercial.tb_direccion_entrega llegada ON llegada.id_direc_entrega = ds.id_llegada
                LEFT OUTER JOIN public.tcdistritos AS dist_lleg ON dist_lleg.co_distri=llegada.co_distri
                LEFT OUTER JOIN configuracion.empresa AS company ON company.id_empresa=1
                LEFT OUTER JOIN rrhh.tctipo_doc AS td_comp ON td_comp.id_tipodoc = company.id_tipdoc
                LEFT OUTER JOIN configuracion.sucursal AS sucursal ON sucursal.id_empresa=company.id_empresa AND sucursal.id_sucursal=1
                LEFT OUTER JOIN public.tcdistritos AS dist_comp ON dist_comp.co_distri=sucursal.co_distri
                WHERE c.id_comprobante = :idComprobante
                """;
    }

    /**
     * Construye la query para obtener los detalles de guía de remisión.
     */
    private String buildQueryDetallesGuiaRemision() {
        return """
                SELECT detail_compr.id_det_comprobante, detail_compr.id_producto, art.cod_articulo, art.desc_articulo, uni.cod_unidad
                , detail_compr.peso AS peso
                FROM facturacion.tbdet_comprobantes AS detail_compr
                LEFT OUTER JOIN logistica.tbarticulos AS art ON art.id_articulo =  detail_compr.id_producto
                LEFT OUTER JOIN logistica.tbunidad AS uni ON uni.id_unidad=art.id_unidad
                WHERE detail_compr.id_comprobante = :idComprobante
                """;
    }

    /**
     * Mapea una fila de la base de datos a GuiaRemisionCabeceraProjection.
     */
    private GuiaRemisionCabeceraProjection mapRowToCabeceraProjection(Row row) {
        return GuiaRemisionCabeceraProjection.builder()
                .idComprobante(row.get("id_comprobante", Integer.class))
                .nuCompro(row.get("nu_compro", String.class))
                .nuSerie(row.get("nu_serie", String.class))
                .nroComprobante(row.get("nro_comprobante", String.class))
                .feEmision(row.get("fe_emision", LocalDate.class))
                .numDocumento(row.get("num_documento", String.class))
                .razonSocial(row.get("company_razon_social", String.class))
                .companyDireccion(row.get("company_direccion", String.class))
                .tlfPrincipal(row.get("tlf_principal", String.class))
                .numFax(row.get("num_fax", String.class))
                .tlfSecundario(row.get("tlf_secundario", String.class))
                .codTipodoc(row.get("cod_tipodoc", String.class))
                .abrevDoc(row.get("abrev_doc", String.class))
                .nuRuc(row.get("nu_ruc", String.class))
                .rznSocial(row.get("rzn_social", String.class))
                .direccion(row.get("no_dir", String.class))
                .codMotivoTraslado(row.get("cod_motivo_traslado", String.class))
                .descMotivoTraslado(row.get("desc_motivo_traslado", String.class))
                .codModTraslado(row.get("cod_mod_traslado", String.class))
                .descModTraslado(row.get("desc_mod_traslado", String.class))
                .docTranspCodTipodoc(row.get("tip_doc_transp", String.class))
                .abrevDocTransp(row.get("abrev_doc_transp", String.class))
                .transpNumTipoDocumento(row.get("num_tipo_documento", String.class))
                .transpRazonSocial(row.get("razon_social", String.class))
                .direcLlegada(row.get("direc_llegada", String.class))
                .ubigeoLlegada(row.get("ubigeo_llegada", String.class))
                .direcPartida(row.get("direc_partida", String.class))
                .ubigeoPartida(row.get("ubigeo_partida", String.class))
                .numPlaca(row.get("num_placa", String.class))
                .codTipodocConductor(row.get("cod_tipodoc_conductor", String.class))
                .tipoDocConductor(row.get("tipo_doc_conductor", String.class))
                .abrevDocConductor(row.get("abrev_doc_conductor", String.class))
                .numDocumentoConductor(row.get("num_documento_conductor", String.class))
                .nombresConductor(row.get("nombres_conductor", String.class))
                .apellidosConductor(row.get("apellidos_conductor", String.class))
                .numLicenciaConductor(row.get("num_licencia_conductor", String.class))
                .detalles(new ArrayList<>())
                .build();
    }

    /**
     * Mapea una fila de la base de datos a GuiaRemisionDetalleProjection.
     */
    private GuiaRemisionDetalleProjection mapRowToDetalleProjection(Row row) {
        return GuiaRemisionDetalleProjection.builder()
                .idDetComprobante(row.get("id_det_comprobante", Integer.class))
                .idProducto(row.get("id_producto", Integer.class))
                .codArticulo(row.get("cod_articulo", String.class))
                .descArticulo(row.get("desc_articulo", String.class))
                .codUnidad(row.get("cod_unidad", String.class))
                .peso(row.get("peso", Double.class))
                .build();
    }
}