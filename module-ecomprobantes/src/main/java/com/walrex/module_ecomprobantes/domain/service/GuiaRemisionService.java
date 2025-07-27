package com.walrex.module_ecomprobantes.domain.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.walrex.module_ecomprobantes.application.ports.input.GenerarPDFGuiaRemisionUseCase;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionDataPort;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionTemplatePort;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionCabeceraProjection;
import com.walrex.module_ecomprobantes.domain.model.dto.ReferralGuideDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.GuiaRemisionProjectionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para la generación de PDFs de guías de remisión.
 * Reutiliza la lógica de GenerarHTMLGuiaRemisionService para mantener
 * consistencia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuiaRemisionService implements GenerarPDFGuiaRemisionUseCase {

    private final GuiaRemisionDataPort guiaRemisionDataPort;
    private final GuiaRemisionTemplatePort guiaRemisionTemplatePort;
    private final GuiaRemisionProjectionMapper guiaRemisionProjectionMapper;

    @Override
    public Mono<byte[]> generarPDFGuiaRemision(Integer idComprobante) {
        log.info("🚀 Iniciando generación de PDF de guía de remisión para comprobante: {}", idComprobante);

        return validarIdComprobante(idComprobante)
                .then(obtenerProyeccionGuiaRemision(idComprobante))
                .flatMap(this::enriquecerDatos)
                .flatMap(this::generarPDF)
                .doOnSuccess(pdf -> log.info("✅ PDF de guía de remisión generado exitosamente para comprobante: {}",
                        idComprobante))
                .doOnError(error -> log.error("❌ Error generando PDF de guía de remisión: {}", error.getMessage()));
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
     * Obtiene la proyección de datos de la guía de remisión desde la base de datos.
     */
    private Mono<GuiaRemisionCabeceraProjection> obtenerProyeccionGuiaRemision(Integer idComprobante) {
        return guiaRemisionDataPort.obtenerProyeccionGuiaRemision(idComprobante)
                .doOnNext(data -> log.debug("📊 Proyección obtenida para comprobante: {}", idComprobante));
    }

    /**
     * Enriquece los datos con información adicional y validaciones de negocio.
     * Convierte la proyección a ReferralGuideDTO usando el mapper.
     * Misma lógica que GenerarHTMLGuiaRemisionService para mantener consistencia.
     */
    private Mono<ReferralGuideDTO> enriquecerDatos(GuiaRemisionCabeceraProjection projection) {
        return Mono.fromCallable(() -> {
            // Convertir proyección a ReferralGuideDTO usando el mapper
            ReferralGuideDTO referralGuideDTO = guiaRemisionProjectionMapper.toReferralGuideDTO(projection);
            referralGuideDTO.setIdVersion("2022");

            // Establecer fecha de emisión si no existe
            if (referralGuideDTO.getFecEmision() == null) {
                referralGuideDTO.setFecEmision(LocalDate.now());
            }

            // Generar número de guía si no existe
            if (referralGuideDTO.getNumCorrelativo() == null) {
                referralGuideDTO.setNumCorrelativo(generarNumeroCorrelativo(projection));
            }

            // Validar que todos los datos requeridos estén presentes
            validarDatosCompletos(referralGuideDTO);

            log.debug("🔧 Datos enriquecidos para PDF de guía: {}", referralGuideDTO);
            return referralGuideDTO;
        });
    }

    /**
     * Genera el PDF usando el template con ReferralGuideDTO.
     * Usa el mismo HTML que funciona para el endpoint de HTML.
     */
    private Mono<byte[]> generarPDF(ReferralGuideDTO data) {
        return guiaRemisionTemplatePort.generarPDF(data)
                .map(pdfStream -> {
                    try {
                        byte[] pdfBytes = pdfStream.toByteArray();
                        log.debug("📄 PDF generado para guía: {} ({} bytes)", data.getNumCorrelativo(),
                                pdfBytes.length);
                        return pdfBytes;
                    } catch (Exception e) {
                        log.error("❌ Error convirtiendo PDF a bytes: {}", e.getMessage());
                        throw new RuntimeException("Error procesando PDF", e);
                    }
                });
    }

    /**
     * Genera un número único de correlativo para la guía de remisión.
     */
    private Integer generarNumeroCorrelativo(GuiaRemisionCabeceraProjection projection) {
        return projection.getIdComprobante() != null ? projection.getIdComprobante() : 1;
    }

    /**
     * Valida que todos los datos necesarios estén completos.
     */
    private void validarDatosCompletos(ReferralGuideDTO data) {
        if (data.getCompany() == null) {
            throw new IllegalStateException("Los datos de la empresa son obligatorios");
        }
        if (data.getReceiver() == null) {
            throw new IllegalStateException("Los datos del destinatario son obligatorios");
        }
        if (data.getShipment() == null) {
            throw new IllegalStateException("Los datos del envío son obligatorios");
        }
        if (data.getDetalle() == null || data.getDetalle().isEmpty()) {
            throw new IllegalStateException("Los detalles del envío son obligatorios");
        }
    }
}