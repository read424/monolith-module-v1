package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionTemplatePort;
import com.walrex.module_ecomprobantes.domain.model.dto.ReferralGuideDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador de template para la generación de PDFs de guías de remisión.
 * Implementa la generación usando Thymeleaf y OpenHTMLtoPDF.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuiaRemisionTemplateAdapter implements GuiaRemisionTemplatePort {

    private final TemplateEngine templateEngine;

    private static final String TEMPLATE_NAME = "comprobantes/guia-remision";
    private static final String LOGO_PATH = "static/assets/sector-logn.jpeg";

    @Override
    public Mono<ByteArrayOutputStream> generarPDF(ReferralGuideDTO data) {
        log.info("📄 Generando PDF para guía de remisión: {}", data.getNumCorrelativo());

        return generarHTML(data)
                .flatMap(html -> convertirHTMLaPDF(html))
                .doOnSuccess(pdf -> log.info("✅ PDF generado exitosamente para guía: {}", data.getNumCorrelativo()))
                .doOnError(error -> log.error("❌ Error generando PDF: {}", error.getMessage()));
    }

    @Override
    public Mono<String> generarHTML(ReferralGuideDTO data) {
        return Mono.fromCallable(() -> {
            Context context = new Context();

            // Formatear el correlativo con 8 ceros a la derecha
            String correlativoFormateado = String.format("%08d", data.getNumCorrelativo());
            data.setNumCorrelativo(Integer.parseInt(correlativoFormateado));

            context.setVariable("doc", data);
            context.setVariable("name", "GUÍA DE REMISIÓN");

            // Agregar datos adicionales
            context.setVariable("company", data.getCompany());
            context.setVariable("receiver", data.getReceiver());
            context.setVariable("direccion_receiver", data.getReceiver().getDireccion());
            context.setVariable("observacion", data.getObservacion());
            context.setVariable("fecEmision", data.getFecEmision());

            // Agregar datos del envío si existen
            if (data.getShipment() != null) {
                context.setVariable("shipment", data.getShipment());
                context.setVariable("transportista", data.getShipment().getTransportista());
                context.setVariable("vehiculo_principal", data.getShipment().getVehiculos().getPrincipal());
                context.setVariable("conductor", data.getShipment().getConductor());
                context.setVariable("llegada", data.getShipment().getLlegada());
                context.setVariable("partida", data.getShipment().getPartida());
            }

            // Agregar detalles si existen
            if (data.getDetalle() != null && !data.getDetalle().isEmpty()) {
                context.setVariable("detalles", data.getDetalle());
            }

            // Variables adicionales que puede necesitar el template
            context.setVariable("params", Map.of(
                    "user", Map.of(
                            "header", "",
                            "footer", ""),
                    "system", Map.of(
                            "logo", getLogoAsBase64(),
                            "hash", "ABCD1234")));

            log.debug("🔧 Contexto preparado para template: {}", TEMPLATE_NAME);
            return templateEngine.process(TEMPLATE_NAME, context);
        });
    }

    /**
     * Lee el logo desde el archivo y lo convierte a base64.
     * Detecta automáticamente el tipo de imagen (PNG, JPG, JPEG, etc.).
     * Si no encuentra el archivo, usa un placeholder.
     */
    private String getLogoAsBase64() {
        try {
            Resource resource = new ClassPathResource(LOGO_PATH);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String base64 = Base64.getEncoder().encodeToString(bytes);

            // Detectar el tipo de imagen basado en los primeros bytes (magic numbers)
            String mimeType = detectImageMimeType(bytes);

            log.debug("✅ Logo cargado exitosamente desde: {} (tipo: {})", LOGO_PATH, mimeType);
            return "data:" + mimeType + ";base64," + base64;

        } catch (IOException e) {
            log.warn("⚠️ No se pudo cargar el logo desde {}, usando placeholder: {}", LOGO_PATH, e.getMessage());
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        }
    }

    /**
     * Detecta el tipo MIME de la imagen basado en los magic numbers.
     * Soporta PNG, JPG, JPEG, GIF, WebP.
     */
    private String detectImageMimeType(byte[] bytes) {
        if (bytes.length < 4) {
            return "image/png"; // Default fallback
        }

        // PNG: 89 50 4E 47
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        // JPEG: FF D8 FF
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // GIF: 47 49 46 38 (GIF8)
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
            return "image/gif";
        }

        // WebP: 52 49 46 46 ... 57 45 42 50
        if (bytes.length >= 12 &&
                bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46 &&
                bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return "image/webp";
        }

        // Si no se reconoce, asumir PNG como fallback
        log.warn("⚠️ Tipo de imagen no reconocido, usando PNG como fallback");
        return "image/png";
    }

    /**
     * Convierte HTML a PDF usando OpenHTMLtoPDF.
     */
    private Mono<ByteArrayOutputStream> convertirHTMLaPDF(String html) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

            try {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withUri("data:text/html;charset=utf-8," + html);
                builder.toStream(pdfOutputStream);
                builder.run();

                log.debug("📄 HTML convertido a PDF exitosamente");
                return pdfOutputStream;

            } catch (IOException e) {
                log.error("❌ Error convirtiendo HTML a PDF: {}", e.getMessage());
                throw new RuntimeException("Error generando PDF", e);
            }
        });
    }
}