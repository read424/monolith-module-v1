package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.walrex.module_ecomprobantes.application.ports.output.GuiaRemisionTemplatePort;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionDataDTO;

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

    @Override
    public Mono<ByteArrayOutputStream> generarPDF(GuiaRemisionDataDTO data) {
        log.info("📄 Generando PDF para guía de remisión: {}", data.getNumeroGuia());

        return generarHTML(data)
                .flatMap(html -> convertirHTMLaPDF(html))
                .doOnSuccess(pdf -> log.info("✅ PDF generado exitosamente para guía: {}", data.getNumeroGuia()))
                .doOnError(error -> log.error("❌ Error generando PDF: {}", error.getMessage()));
    }

    @Override
    public Mono<String> generarHTML(GuiaRemisionDataDTO data) {
        return Mono.fromCallable(() -> {
            Context context = new Context();

            // Agregar datos principales
            context.setVariable("guia", data);
            context.setVariable("client", data.getClient());
            context.setVariable("company", data.getCompany());
            context.setVariable("carrier", data.getCarrier());
            context.setVariable("driver", data.getDriver());
            context.setVariable("vehicle", data.getVehicle());
            context.setVariable("shipment", data.getShipment());
            context.setVariable("despatch", data.getDespatch());

            // Agregar datos adicionales
            context.setVariable("fechaEmision", data.getFechaEmision());
            context.setVariable("fechaEntrega", data.getFechaEntrega());
            context.setVariable("numeroGuia", data.getNumeroGuia());
            context.setVariable("observaciones", data.getObservaciones());

            // Agregar detalles si existen
            if (data.getDetalles() != null && !data.getDetalles().isEmpty()) {
                context.setVariable("detalles", data.getDetalles());
            }

            // Agregar direcciones si existen
            if (data.getDireccionOrigen() != null) {
                context.setVariable("direccionOrigen", data.getDireccionOrigen());
            }
            if (data.getDireccionDestino() != null) {
                context.setVariable("direccionDestino", data.getDireccionDestino());
            }
            if (data.getDireccionTransportista() != null) {
                context.setVariable("direccionTransportista", data.getDireccionTransportista());
            }

            log.debug("🔧 Contexto preparado para template: {}", TEMPLATE_NAME);
            return templateEngine.process(TEMPLATE_NAME, context);
        });
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