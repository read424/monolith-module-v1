package com.walrex.module_ecomprobantes.domain.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.walrex.module_ecomprobantes.application.ports.input.GenerarGuiaRemisionUseCase;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionDataPort;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionTemplatePort;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionDataDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para la generación de guías de remisión.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuiaRemisionService implements GenerarGuiaRemisionUseCase {

    private final GuiaRemisionDataPort guiaRemisionDataPort;
    private final GuiaRemisionTemplatePort guiaRemisionTemplatePort;

    @Override
    public Mono<ByteArrayOutputStream> generarGuiaRemision(Integer idComprobante) {
        log.info("🚀 Iniciando generación de guía de remisión para comprobante: {}", idComprobante);

        return validarIdComprobante(idComprobante)
                .then(obtenerDatosGuiaRemision(idComprobante))
                .flatMap(this::enriquecerDatos)
                .flatMap(this::generarPDF)
                .doOnSuccess(pdf -> log.info("✅ Guía de remisión generada exitosamente para comprobante: {}",
                        idComprobante))
                .doOnError(error -> log.error("❌ Error generando guía de remisión: {}", error.getMessage()));
    }

    /**
     * Valida que el ID del comprobante sea válido.
     */
    private Mono<Void> validarIdComprobante(Integer idComprobante) {
        return Mono.fromRunnable(() -> {
            if (idComprobante == null || idComprobante <= 0) {
                throw new IllegalArgumentException("El ID del comprobante debe ser válido y mayor a 0");
            }
            log.debug("✅ Validación de ID de comprobante completada");
        });
    }

    /**
     * Obtiene los datos de la guía de remisión desde la base de datos.
     */
    private Mono<GuiaRemisionDataDTO> obtenerDatosGuiaRemision(Integer idComprobante) {
        return guiaRemisionDataPort.obtenerDatosGuiaRemision(idComprobante)
                .doOnNext(data -> log.debug("📊 Datos obtenidos para comprobante: {}", idComprobante));
    }

    /**
     * Enriquece los datos con información adicional y validaciones de negocio.
     */
    private Mono<GuiaRemisionDataDTO> enriquecerDatos(GuiaRemisionDataDTO data) {
        return Mono.fromCallable(() -> {
            // Establecer fecha de emisión si no existe
            if (data.getFechaEmision() == null) {
                data.setFechaEmision(LocalDate.now());
            }

            // Generar número de guía si no existe
            if (data.getNumeroGuia() == null || data.getNumeroGuia().trim().isEmpty()) {
                data.setNumeroGuia(generarNumeroGuia(data));
            }

            // Validar que todos los datos requeridos estén presentes
            validarDatosCompletos(data);

            log.debug("🔧 Datos enriquecidos para guía: {}", data.getNumeroGuia());
            return data;
        });
    }

    /**
     * Genera el PDF usando el template.
     */
    private Mono<ByteArrayOutputStream> generarPDF(GuiaRemisionDataDTO data) {
        return guiaRemisionTemplatePort.generarPDF(data)
                .doOnNext(pdf -> log.debug("📄 PDF generado para guía: {}", data.getNumeroGuia()));
    }

    /**
     * Genera un número único de guía de remisión.
     */
    private String generarNumeroGuia(GuiaRemisionDataDTO data) {
        String serie = data.getSerieGuia() != null ? data.getSerieGuia() : "GR";
        String correlativo = data.getCorrelativoGuia() != null ? data.getCorrelativoGuia()
                : String.format("%06d", data.getIdOrdenSalida());
        return serie + "-" + correlativo;
    }

    /**
     * Valida que todos los datos necesarios estén completos.
     */
    private void validarDatosCompletos(GuiaRemisionDataDTO data) {
        if (data.getClient() == null) {
            throw new IllegalStateException("Los datos del cliente son obligatorios");
        }
        if (data.getCompany() == null) {
            throw new IllegalStateException("Los datos de la empresa son obligatorios");
        }
        if (data.getDriver() == null) {
            throw new IllegalStateException("Los datos del conductor son obligatorios");
        }
        if (data.getVehicle() == null) {
            throw new IllegalStateException("Los datos del vehículo son obligatorios");
        }
        if (data.getDetalles() == null || data.getDetalles().isEmpty()) {
            throw new IllegalStateException("Los detalles del despacho son obligatorios");
        }
    }
}