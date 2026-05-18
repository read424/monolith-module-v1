package com.walrex.module_laboratorio.infrastructure.adapters.outbound.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.walrex.module_laboratorio.application.ports.output.CurvaDisenoPdfPort;
import com.walrex.module_laboratorio.domain.exceptions.CurvaDisenoException;
import com.walrex.module_laboratorio.domain.exceptions.RecetaException;
import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.domain.model.Receta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurvaDisenoPdfAdapter implements CurvaDisenoPdfPort {

    private static final int PAGE_WIDTH = 1180;
    private static final int PAGE_PADDING = 30;
    private static final int HEADER_HEIGHT = 120;
    private static final int STAGE_GAP = 42;
    private static final int STAGE_HEADER_HEIGHT = 54;
    private static final int STAGE_PADDING = 24;
    private static final int AXIS_LEFT = 56;
    private static final int AXIS_BOTTOM = 36;
    private static final int MIN_STAGE_GRAPH_HEIGHT = 320;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<byte[]> generatePdf(Receta receta) {
        return Mono.fromCallable(() -> {
            List<com.walrex.module_laboratorio.domain.model.CurvaDisenoItem> curvas = receta.getCurvaDiseno();
            if (curvas == null || curvas.isEmpty()) {
                throw new RecetaException("La receta no tiene curvas de diseño registradas", "CURVA_DISENO_EMPTY");
            }
            if (curvas.size() == 1) {
                JsonNode root = parseJson(curvas.get(0).getCurva(), "RECETA");
                String html = buildHtml(ChartMetadata.fromReceta(receta), root);
                return renderPdf(html, "RECETA");
            }
            try (PDDocument merged = new PDDocument();
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                for (com.walrex.module_laboratorio.domain.model.CurvaDisenoItem item : curvas) {
                    JsonNode root = parseJson(item.getCurva(), "RECETA");
                    String html = buildHtml(ChartMetadata.fromReceta(receta), root);
                    byte[] pdfBytes = renderPdf(html, "RECETA");
                    try (PDDocument part = PDDocument.load(pdfBytes)) {
                        for (PDPage page : part.getPages()) {
                            merged.addPage(merged.importPage(page));
                        }
                    }
                }
                merged.save(output);
                return output.toByteArray();
            } catch (RecetaException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Error mergeando PDFs de curvas de diseño", ex);
                throw new RecetaException("No se pudo generar el PDF de la receta", "PDF_GENERATION_ERROR");
            }
        });
    }

    @Override
    public Mono<byte[]> generatePdf(CurvaDiseno curvaDiseno) {
        return Mono.fromCallable(() -> renderSimpleCurvaDisenoPdf(curvaDiseno));
    }

    private JsonNode parseJson(String curvaDiseno, String source) {
        try {
            return objectMapper.readTree(curvaDiseno);
        } catch (Exception ex) {
            if ("CURVA_DISENO".equals(source)) {
                throw new CurvaDisenoException("La curva_diseno no contiene un JSON válido", "INVALID_CURVA_DISENO");
            }
            throw new RecetaException("La curva_diseno no contiene un JSON válido", "INVALID_CURVA_DISENO");
        }
    }

    private byte[] renderPdf(String html) {
        return renderPdf(html, "RECETA");
    }

    private byte[] renderPdf(String html, String source) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            validateXml(html, source);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (CurvaDisenoException | RecetaException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error generando PDF de curva_diseno", ex);
            if ("CURVA_DISENO".equals(source)) {
                throw new CurvaDisenoException("No se pudo generar el PDF de curva_diseno", "PDF_GENERATION_ERROR");
            }
            throw new RecetaException("No se pudo generar el PDF de curva_diseno", "PDF_GENERATION_ERROR");
        }
    }

    private byte[] renderSimpleCurvaDisenoPdf(CurvaDiseno curvaDiseno) {
        try (PDDocument document = new PDDocument();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JsonNode root = parseJson(curvaDiseno.getCurvaDiseno(), "CURVA_DISENO");
            List<PdfSegment> segments = extractThermalSegments(root);
            List<PdfTimelineEvent> timelineEvents = extractTimelineEvents(root);

            PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeA4);
            document.addPage(page);

            float pageHeight = landscapeA4.getHeight();
            float margin = 42;
            float chartX = margin;
            float chartY = 86;
            float chartWidth = landscapeA4.getWidth() - margin * 2;
            float chartHeight = pageHeight - 170;
            float plotPadding = 34;
            float plotX = chartX + plotPadding;
            float plotY = chartY + plotPadding;
            float plotWidth = chartWidth - plotPadding * 2;
            float plotHeight = chartHeight - plotPadding * 2;

            String descripcion = valueOrDefault(curvaDiseno.getDescripcion(), "Curva de Diseno");
            String version = valueOrDefault(curvaDiseno.getVersion(), "N/E");
            String laboratorista = valueOrDefault(curvaDiseno.getLaboratorista(), "N/E");
            String subtitle = "ID " + curvaDiseno.getId() + " | " + descripcion
                    + " | Version: " + version
                    + " | Laboratorista: " + laboratorista;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setNonStrokingColor(new Color(248, 250, 252));
                content.addRect(0, 0, landscapeA4.getWidth(), pageHeight);
                content.fill();

                content.setNonStrokingColor(new Color(15, 23, 42));
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(margin, pageHeight - 48);
                content.showText("Curva de Diseno");
                content.endText();

                content.setNonStrokingColor(new Color(71, 85, 105));
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(margin, pageHeight - 64);
                content.showText(normalizePdfText(subtitle));
                content.endText();

                content.setNonStrokingColor(Color.WHITE);
                content.addRect(chartX, chartY, chartWidth, chartHeight);
                content.fill();

                if (segments.isEmpty()) {
                    content.setNonStrokingColor(new Color(100, 116, 139));
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 11);
                    content.newLineAtOffset(plotX, chartY + chartHeight - 54);
                    content.showText("No se encontraron segmentos en stages[].thermalProfile.segments[].coordinates");
                    content.endText();
                } else {
                    PdfBounds bounds = PdfBounds.from(segments, timelineEvents);
                    drawThermalSegments(content, segments, bounds, plotX, plotY, plotWidth, plotHeight);
                    drawTimelineEvents(content, timelineEvents, segments, bounds, plotX, plotY, plotWidth, plotHeight);
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (CurvaDisenoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error generando PDF simple de curva_diseno", ex);
            throw new CurvaDisenoException("No se pudo generar el PDF de curva_diseno", "PDF_GENERATION_ERROR");
        }
    }

    private List<PdfSegment> extractThermalSegments(JsonNode root) {
        List<PdfSegment> segments = new ArrayList<>();
        JsonNode stages = root.path("stages");
        if (!stages.isArray()) {
            return segments;
        }

        for (JsonNode stage : stages) {
            String stageAlias = valueOrDefault(stage.path("alias").asText(null), stage.path("id").asText("stage"));
            JsonNode thermalSegments = stage.path("thermalProfile").path("segments");
            if (!thermalSegments.isArray()) {
                continue;
            }
            for (JsonNode segment : thermalSegments) {
                JsonNode coordinates = segment.path("coordinates");
                if (hasPoint(coordinates.path("start")) && hasPoint(coordinates.path("end"))) {
                    segments.add(new PdfSegment(
                            stageAlias,
                            segment.path("type").asText(""),
                            new PdfPoint(coordinates.path("start").path("x").asDouble(),
                                    coordinates.path("start").path("y").asDouble()),
                            new PdfPoint(coordinates.path("end").path("x").asDouble(),
                                    coordinates.path("end").path("y").asDouble()),
                            segment.path("startTemperature").asText(""),
                            segment.path("endTemperature").asText(""),
                            segment.path("prevSegmentId").asText("")
                    ));
                }
            }
        }
        return segments;
    }

    private List<PdfTimelineEvent> extractTimelineEvents(JsonNode root) {
        List<PdfTimelineEvent> events = new ArrayList<>();
        JsonNode stages = root.path("stages");
        if (!stages.isArray()) {
            return events;
        }

        for (JsonNode stage : stages) {
            String stageAlias = valueOrDefault(stage.path("alias").asText(null), stage.path("id").asText("stage"));
            JsonNode timeline = stage.path("timeline");
            if (!timeline.isArray()) {
                continue;
            }
            for (JsonNode event : timeline) {
                JsonNode coordinates = event.path("coordinates");
                if (hasPoint(coordinates.path("start")) && hasPoint(coordinates.path("end"))) {
                    events.add(new PdfTimelineEvent(
                            stageAlias,
                            event.path("type").asText(""),
                            new PdfPoint(coordinates.path("start").path("x").asDouble(),
                                    coordinates.path("start").path("y").asDouble()),
                            new PdfPoint(coordinates.path("end").path("x").asDouble(),
                                    coordinates.path("end").path("y").asDouble()),
                            buildEventLabel(event),
                            event.path("requiresConfirmation").asBoolean(false),
                            event.path("originOffset").path("dy").asDouble(0)
                    ));
                }
            }
        }
        return events;
    }

    private boolean hasPoint(JsonNode point) {
        return point.path("x").isNumber() && point.path("y").isNumber();
    }

    private void drawThermalSegments(PDPageContentStream content, List<PdfSegment> segments, PdfBounds bounds,
                                     float plotX, float plotY, float plotWidth, float plotHeight) throws Exception {
        for (int i = 0; i < segments.size(); i++) {
            PdfSegment segment = segments.get(i);
            float x1 = mapPdfX(segment.start().x(), bounds, plotX, plotWidth);
            float y1 = mapPdfY(segment.start().y(), bounds, plotY, plotHeight);
            float x2 = mapPdfX(segment.end().x(), bounds, plotX, plotWidth);
            float y2 = mapPdfY(segment.end().y(), bounds, plotY, plotHeight);

            content.setStrokingColor(Color.BLACK);
            content.setLineWidth(2f);
            content.moveTo(x1, y1);
            content.lineTo(x2, y2);
            content.stroke();

            content.setNonStrokingColor(Color.BLACK);
            content.addRect(x1 - 1.8f, y1 - 1.8f, 3.6f, 3.6f);
            content.fill();
            content.addRect(x2 - 1.8f, y2 - 1.8f, 3.6f, 3.6f);
            content.fill();

            drawThermalTemperatureLabels(content, segment, x1, y1, x2, y2);
        }
    }

    private void drawTimelineEvents(PDPageContentStream content, List<PdfTimelineEvent> events,
                                    List<PdfSegment> segments, PdfBounds bounds,
                                    float plotX, float plotY, float plotWidth, float plotHeight) throws Exception {
        List<PdfLine> thermalLines = segmentsToReference(segments, bounds, plotX, plotY, plotWidth, plotHeight);
        for (int i = 0; i < events.size(); i++) {
            PdfTimelineEvent event = events.get(i);
            float x1 = mapPdfX(event.start().x(), bounds, plotX, plotWidth);
            float y1 = mapPdfY(event.start().y(), bounds, plotY, plotHeight);
            float x2 = mapPdfX(event.end().x(), bounds, plotX, plotWidth);
            float y2 = mapPdfY(event.end().y(), bounds, plotY, plotHeight);

            PdfLine line = orientTimelineLine(event, x1, y1, x2, y2, thermalLines);
            drawArrowLine(content, line.x1(), line.y1(), line.x2(), line.y2());

            PdfPoint labelPoint = calculateTimelineLabelPoint(event, line, x1, y1, plotY, plotHeight);
            float labelX = (float) labelPoint.x();
            float labelY = (float) labelPoint.y();
            String label = truncate(normalizePdfText(event.label()), 95);
            if (!label.isBlank()) {
                drawPdfText(content, label, labelX, labelY, 7);
            }

            if (event.requiresConfirmation()) {
                drawPdfText(content, "Requiere confirmacion", labelX, labelY - 9, 7);
            }
        }
    }

    private PdfPoint calculateTimelineLabelPoint(PdfTimelineEvent event, PdfLine line,
                                                float startX, float startY,
                                                float plotY, float plotHeight) {
        if (isInstantEvent(event)) {
            float labelY = event.originOffsetDy() > 0
                    ? Math.max(plotY + 8, startY - 14)
                    : Math.min(plotY + plotHeight - 8, startY + 14);
            return new PdfPoint(startX + 6, labelY);
        }
        if (isDrainEvent(event)) {
            float labelX = line.x2() + 6;
            float labelY = Math.max(plotY + 8, line.y2() - 14);
            return new PdfPoint(labelX, labelY);
        }

        float labelY;
        labelY = Math.max(line.y1(), line.y2()) + 8;
        return new PdfPoint(Math.min(line.x1(), line.x2()) + 6, keepLabelInsidePlot(labelY, line, plotY, plotHeight));
    }

    private float keepLabelInsidePlot(float labelY, PdfLine line, float plotY, float plotHeight) {
        if (labelY > plotY + plotHeight - 8) {
            return Math.min(line.y1(), line.y2()) - 12;
        }
        if (labelY < plotY + 8) {
            return Math.max(line.y1(), line.y2()) + 8;
        }
        return labelY;
    }

    private void drawThermalTemperatureLabels(PDPageContentStream content, PdfSegment segment,
                                              float x1, float y1, float x2, float y2) throws Exception {
        String type = segment.type().toLowerCase(java.util.Locale.ROOT);
        if ("hold".equals(type) && segment.prevSegmentId().isBlank() && !segment.startTemperature().isBlank()) {
            drawPdfText(content, normalizePdfText(segment.startTemperature() + " grados"), x1 - 24, y1 + 10, 9);
        }
        if ("ramp".equals(type) && !segment.endTemperature().isBlank()) {
            drawPdfText(content, normalizePdfText(segment.endTemperature() + " grados"), x2 + 6, y2 + 10, 9);
        }
    }

    private PdfLine orientTimelineLine(PdfTimelineEvent event, float x1, float y1, float x2, float y2,
                                       List<PdfLine> thermalLines) {
        if (isInstantEvent(event)) {
            return orientToNearestThermalLine(x1, y1, x2, y2, thermalLines);
        }
        if (isDrainEvent(event)) {
            return orientToFarthestThermalLine(x1, y1, x2, y2, thermalLines);
        }
        return new PdfLine(x1, y1, x2, y2);
    }

    private List<PdfLine> segmentsToReference(List<PdfSegment> segments, PdfBounds bounds, float plotX, float plotY,
                                              float plotWidth, float plotHeight) {
        return segments.stream()
                .map(segment -> new PdfLine(
                        mapPdfX(segment.start().x(), bounds, plotX, plotWidth),
                        mapPdfY(segment.start().y(), bounds, plotY, plotHeight),
                        mapPdfX(segment.end().x(), bounds, plotX, plotWidth),
                        mapPdfY(segment.end().y(), bounds, plotY, plotHeight)
                ))
                .toList();
    }

    private PdfLine orientToNearestThermalLine(float x1, float y1, float x2, float y2, List<PdfLine> thermalLines) {
        double startDistance = minDistanceToThermalLines(x1, y1, thermalLines);
        double endDistance = minDistanceToThermalLines(x2, y2, thermalLines);
        return endDistance <= startDistance ? new PdfLine(x1, y1, x2, y2) : new PdfLine(x2, y2, x1, y1);
    }

    private PdfLine orientToFarthestThermalLine(float x1, float y1, float x2, float y2, List<PdfLine> thermalLines) {
        double startDistance = minDistanceToThermalLines(x1, y1, thermalLines);
        double endDistance = minDistanceToThermalLines(x2, y2, thermalLines);
        return endDistance >= startDistance ? new PdfLine(x1, y1, x2, y2) : new PdfLine(x2, y2, x1, y1);
    }

    private double minDistanceToThermalLines(float x, float y, List<PdfLine> thermalLines) {
        return thermalLines.stream()
                .mapToDouble(line -> distancePointToSegment(x, y, line))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private double distancePointToSegment(float x, float y, PdfLine line) {
        double dx = line.x2() - line.x1();
        double dy = line.y2() - line.y1();
        if (dx == 0 && dy == 0) {
            return Math.hypot(x - line.x1(), y - line.y1());
        }
        double t = ((x - line.x1()) * dx + (y - line.y1()) * dy) / (dx * dx + dy * dy);
        double clamped = Math.max(0, Math.min(1, t));
        double closestX = line.x1() + clamped * dx;
        double closestY = line.y1() + clamped * dy;
        return Math.hypot(x - closestX, y - closestY);
    }

    private boolean isInstantEvent(PdfTimelineEvent event) {
        return containsAny(event.type(), "instant") || containsAny(event.label(), "instantaneo", "instantáneo");
    }

    private boolean isDrainEvent(PdfTimelineEvent event) {
        return containsAny(event.type(), "drain", "drenaje") || containsAny(event.label(), "drain", "drenaje");
    }

    private boolean containsAny(String value, String... tokens) {
        String normalized = normalizeForMatch(value);
        for (String token : tokens) {
            if (normalized.contains(normalizeForMatch(token))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeForMatch(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private void drawArrowLine(PDPageContentStream content, float x1, float y1, float x2, float y2) throws Exception {
        content.setStrokingColor(Color.BLACK);
        content.setLineWidth(1.8f);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();

        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowAngle = Math.toRadians(32);
        float arrowLength = 10f;
        float ax1 = x2 - (float) (Math.cos(angle - arrowAngle) * arrowLength);
        float ay1 = y2 - (float) (Math.sin(angle - arrowAngle) * arrowLength);
        float ax2 = x2 - (float) (Math.cos(angle + arrowAngle) * arrowLength);
        float ay2 = y2 - (float) (Math.sin(angle + arrowAngle) * arrowLength);

        content.moveTo(x2, y2);
        content.lineTo(ax1, ay1);
        content.moveTo(x2, y2);
        content.lineTo(ax2, ay2);
        content.stroke();
    }

    private void drawPdfText(PDPageContentStream content, String text, float x, float y, int fontSize) throws Exception {
        content.setNonStrokingColor(Color.BLACK);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private float mapPdfX(double value, PdfBounds bounds, float plotX, float plotWidth) {
        return plotX + (float) ((value - bounds.minX()) / bounds.width()) * plotWidth;
    }

    private float mapPdfY(double value, PdfBounds bounds, float plotY, float plotHeight) {
        return plotY + plotHeight - (float) ((value - bounds.minY()) / bounds.height()) * plotHeight;
    }

    private String formatCoordinate(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String buildHtml(ChartMetadata metadata, JsonNode root) {
        JsonNode stages = root.path("stages");
        Bounds bounds = calculateBounds(stages);

        int graphWidth = PAGE_WIDTH - (PAGE_PADDING * 2) - (AXIS_LEFT + STAGE_PADDING * 2);
        int stageGraphHeight = Math.max(bounds.maxY - bounds.minY + 100, MIN_STAGE_GRAPH_HEIGHT);
        int stageBoxHeight = STAGE_HEADER_HEIGHT + stageGraphHeight + AXIS_BOTTOM + STAGE_PADDING;
        int totalStages = Math.max(stages.isArray() ? stages.size() : 0, 1);
        int svgHeight = HEADER_HEIGHT + 70 + totalStages * stageBoxHeight + Math.max(totalStages - 1, 0) * STAGE_GAP;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(PAGE_WIDTH)
                .append("' height='").append(svgHeight)
                .append("' viewBox='0 0 ").append(PAGE_WIDTH).append(" ").append(svgHeight).append("'>");

        appendDefs(svg);
        appendPageBackground(svg, svgHeight);
        appendHeader(svg, metadata, root);
        appendLegend(svg, HEADER_HEIGHT);

        int currentY = HEADER_HEIGHT + 70;
        if (stages.isArray() && !stages.isEmpty()) {
            for (JsonNode stage : stages) {
                appendStage(svg, stage, bounds, currentY, graphWidth, stageGraphHeight, stageBoxHeight);
                currentY += stageBoxHeight + STAGE_GAP;
            }
        } else {
            svg.append("<text x='").append(PAGE_PADDING).append("' y='").append(currentY + 30)
                    .append("' font-size='18' fill='#8a0f2a'>No hay stages para graficar</text>");
        }

        svg.append("</svg>");
        String chartSvg = addElementLineBreaks(svg.toString());

        return """
                <html lang="es">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4 landscape; margin: 14mm; }
                    body {
                      font-family: Arial, sans-serif;
                      color: #1f2937;
                      background: #f8fafc;
                    }
                    .title {
                      font-size: 24px;
                      font-weight: 700;
                      margin-bottom: 6px;
                      color: #0f172a;
                    }
                    .subtitle {
                      font-size: 12px;
                      color: #475569;
                      margin-bottom: 16px;
                    }
                    .chart-wrap {
                      border: 1px solid #cbd5e1;
                      background: #ffffff;
                      padding: 10px;
                      border-radius: 10px;
                    }
                    svg text { font-family: Arial, sans-serif; }
                  </style>
                </head>
                <body>
                  <div class="title">%s</div>
                  <div class="subtitle">%s</div>
                  <div class="chart-wrap">%s</div>
                </body>
                </html>
                """.formatted(escape(metadata.documentTitle()), escape(metadata.documentSubtitle()), chartSvg);
    }

    private String buildSimpleCurvaDisenoHtml(CurvaDiseno curvaDiseno) {
        String descripcion = valueOrDefault(curvaDiseno.getDescripcion(), "Curva de Diseno");
        String version = valueOrDefault(curvaDiseno.getVersion(), "N/E");
        String laboratorista = valueOrDefault(curvaDiseno.getLaboratorista(), "N/E");

        return """
                <html lang="es">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4 landscape; margin: 14mm; }
                    body {
                      font-family: Arial, sans-serif;
                      color: #1f2937;
                      background: #f8fafc;
                    }
                    .title {
                      font-size: 24px;
                      font-weight: 700;
                      margin-bottom: 6px;
                      color: #0f172a;
                    }
                    .subtitle {
                      font-size: 12px;
                      color: #475569;
                      margin-bottom: 16px;
                    }
                    .chart-wrap {
                      border: 1px solid #cbd5e1;
                      background: #ffffff;
                      padding: 12px;
                      border-radius: 8px;
                    }
                  </style>
                </head>
                <body>
                  <div class="title">Curva de Diseno</div>
                  <div class="subtitle">%s</div>
                  <div class="chart-wrap">
                    <svg width="500" height="500" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
                      <line x1="215" y1="299" x2="277" y2="108" stroke="black" stroke-width="2" />
                    </svg>
                  </div>
                </body>
                </html>
                """.formatted(
                escape("ID " + curvaDiseno.getId() + " | " + descripcion
                        + " | Version: " + version
                        + " | Laboratorista: " + laboratorista)
        );
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String normalizePdfText(String value) {
        return value == null ? "" : value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace("°", " grados ");
    }

    private String addElementLineBreaks(String markup) {
        return markup.replace("><", ">\n<");
    }

    private void validateXml(String html, String source) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.newDocumentBuilder().parse(new InputSource(new StringReader(html)));
        } catch (Exception ex) {
            String context = buildXmlContext(html, ex);
            log.error("SVG/HTML inválido antes de renderizar PDF. {}", context, ex);
            if ("CURVA_DISENO".equals(source)) {
                throw new CurvaDisenoException("El SVG generado para la curva_diseno no es XML válido. " + context,
                        "PDF_GENERATION_ERROR");
            }
            throw new RecetaException("El SVG generado para la curva_diseno no es XML válido. " + context,
                    "PDF_GENERATION_ERROR");
        }
    }

    private String buildXmlContext(String html, Exception ex) {
        SAXParseException saxParseException = findSaxParseException(ex);
        if (saxParseException != null) {
            List<String> lines = html.lines().toList();
            int lineNumber = saxParseException.getLineNumber();
            int from = Math.max(lineNumber - 3, 0);
            int to = Math.min(lineNumber + 2, lines.size());
            StringBuilder context = new StringBuilder();
            for (int i = from; i < to; i++) {
                context.append(i + 1).append(": ").append(lines.get(i)).append(System.lineSeparator());
            }
            return "Línea " + saxParseException.getLineNumber()
                    + ", columna " + saxParseException.getColumnNumber()
                    + ". Contexto: " + context;
        }
        return "Detalle: " + ex.getMessage();
    }

    private SAXParseException findSaxParseException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SAXParseException saxParseException) {
                return saxParseException;
            }
            current = current.getCause();
        }
        return null;
    }

    private void appendDefs(StringBuilder svg) {
        svg.append("""
                <defs>
                  <linearGradient id='headerGradient' x1='0%' y1='0%' x2='100%' y2='0%'>
                    <stop offset='0%' stop-color='#0f172a'/>
                    <stop offset='100%' stop-color='#1d4ed8'/>
                  </linearGradient>
                  <linearGradient id='cardGradient' x1='0%' y1='0%' x2='100%' y2='100%'>
                    <stop offset='0%' stop-color='#ffffff'/>
                    <stop offset='100%' stop-color='#f8fafc'/>
                  </linearGradient>
                  <filter id='shadow'>
                    <feDropShadow dx='0' dy='3' stdDeviation='6' flood-color='#94a3b8' flood-opacity='0.18'/>
                  </filter>
                </defs>
                """);
    }

    private void appendPageBackground(StringBuilder svg, int svgHeight) {
        svg.append("<rect x='0' y='0' width='").append(PAGE_WIDTH).append("' height='").append(svgHeight)
                .append("' fill='#f8fafc'/>");
        svg.append("<rect x='18' y='18' width='").append(PAGE_WIDTH - 36).append("' height='").append(svgHeight - 36)
                .append("' rx='18' ry='18' fill='#eef4ff' opacity='0.45'/>");
    }

    private void appendHeader(StringBuilder svg, ChartMetadata metadata, JsonNode root) {
        svg.append("<rect x='").append(PAGE_PADDING).append("' y='").append(PAGE_PADDING)
                .append("' width='").append(PAGE_WIDTH - PAGE_PADDING * 2).append("' height='80'")
                .append(" rx='18' ry='18' fill='url(#headerGradient)' filter='url(#shadow)'/>");

        svg.append("<text x='").append(PAGE_PADDING + 26).append("' y='").append(PAGE_PADDING + 33)
                .append("' font-size='28' font-weight='700' fill='#ffffff'>")
                .append(escape(metadata.headerTitle()))
                .append("</text>");

        svg.append("<text x='").append(PAGE_PADDING + 26).append("' y='").append(PAGE_PADDING + 58)
                .append("' font-size='12' fill='#dbeafe'>")
                .append(escape(metadata.headerSubtitle()))
                .append("</text>");

        if (metadata.version() != null && !metadata.version().isBlank()) {
            svg.append("<text x='").append(PAGE_PADDING + 220).append("' y='").append(PAGE_PADDING + 58)
                    .append("' font-size='12' fill='#dbeafe'>Version: ")
                    .append(escape(metadata.version()))
                    .append("</text>");
        }

        if (root.path("name").isTextual()) {
            svg.append("<text x='").append(PAGE_PADDING + 390).append("' y='").append(PAGE_PADDING + 58)
                    .append("' font-size='12' fill='#dbeafe'>Nombre curva: ")
                    .append(escape(root.path("name").asText()))
                    .append("</text>");
        }

        if (root.path("updatedAt").isTextual()) {
            svg.append("<text x='").append(PAGE_WIDTH - 345).append("' y='").append(PAGE_PADDING + 40)
                    .append("' font-size='12' fill='#dbeafe'>Actualizado: ")
                    .append(escape(root.path("updatedAt").asText()))
                    .append("</text>");
        }
    }

    private void appendLegend(StringBuilder svg, int yBase) {
        int boxX = PAGE_PADDING;
        int boxY = yBase - 6;
        int boxWidth = PAGE_WIDTH - PAGE_PADDING * 2;

        svg.append("<rect x='").append(boxX).append("' y='").append(boxY)
                .append("' width='").append(boxWidth).append("' height='46'")
                .append(" rx='12' ry='12' fill='#ffffff' stroke='#dbe4f0'/>");

        appendLegendItem(svg, boxX + 18, boxY + 27, "#2563eb", "Perfil térmico");
        appendLegendItem(svg, boxX + 210, boxY + 27, "#dc2626", "Evento instantáneo");
        appendLegendDose(svg, boxX + 415, boxY + 27, "#16a34a", "Dosis constante");
        appendLegendTriangle(svg, boxX + 640, boxY + 27, "#7c3aed", "Dosis progresiva");

        svg.append("<text x='").append(boxX + 875).append("' y='").append(boxY + 31)
                .append("' font-size='11' fill='#64748b'>")
                .append("Ejes calculados desde las coordenadas almacenadas")
                .append("</text>");
    }

    private void appendLegendItem(StringBuilder svg, int x, int y, String color, String label) {
        svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x + 28)
                .append("' y2='").append(y).append("' stroke='").append(color).append("' stroke-width='3'/>");
        svg.append("<circle cx='").append(x + 14).append("' cy='").append(y).append("' r='4' fill='").append(color).append("'/>");
        svg.append("<text x='").append(x + 38).append("' y='").append(y + 4)
                .append("' font-size='11' fill='#334155'>").append(label).append("</text>");
    }

    private void appendLegendDose(StringBuilder svg, int x, int y, String color, String label) {
        svg.append("<rect x='").append(x).append("' y='").append(y - 9).append("' width='30' height='18'")
                .append(" fill='").append(color).append("' fill-opacity='0.18' stroke='").append(color).append("' stroke-width='2'/>");
        svg.append("<text x='").append(x + 40).append("' y='").append(y + 4)
                .append("' font-size='11' fill='#334155'>").append(escape(label)).append("</text>");
    }

    private void appendLegendTriangle(StringBuilder svg, int x, int y, String color, String label) {
        svg.append("<polygon points='").append(x).append(",").append(y + 8)
                .append(" ").append(x + 28).append(",").append(y + 8)
                .append(" ").append(x + 28).append(",").append(y - 10)
                .append("' fill='").append(color).append("' fill-opacity='0.16' stroke='").append(color).append("' stroke-width='2'/>");
        svg.append("<text x='").append(x + 40).append("' y='").append(y + 4)
                .append("' font-size='11' fill='#334155'>").append(label).append("</text>");
    }

    private void appendStage(StringBuilder svg, JsonNode stage, Bounds bounds, int yOffset,
                             int graphWidth, int graphHeight, int stageBoxHeight) {
        int stageX = PAGE_PADDING;
        int stageY = yOffset;
        int stageWidth = PAGE_WIDTH - PAGE_PADDING * 2;
        int plotX = stageX + STAGE_PADDING + AXIS_LEFT;
        int plotY = stageY + STAGE_HEADER_HEIGHT + STAGE_PADDING;
        int plotBottomY = plotY + graphHeight;
        int plotRightX = plotX + graphWidth;
        int contentXOffset = plotX - bounds.minX;
        int contentYOffset = plotY - bounds.minY;

        String clipId = "clip-stage-" + escapeId(stage.path("id").asText("stage"));

        svg.append("<rect x='").append(stageX).append("' y='").append(stageY)
                .append("' width='").append(stageWidth).append("' height='").append(stageBoxHeight)
                .append("' rx='18' ry='18' fill='url(#cardGradient)' stroke='#cbd5e1' filter='url(#shadow)'/>");

        svg.append("<rect x='").append(stageX).append("' y='").append(stageY)
                .append("' width='").append(stageWidth).append("' height='").append(STAGE_HEADER_HEIGHT)
                .append("' rx='18' ry='18' fill='#eff6ff'/>");
        svg.append("<rect x='").append(stageX).append("' y='").append(stageY + STAGE_HEADER_HEIGHT - 18)
                .append("' width='").append(stageWidth).append("' height='18' fill='#eff6ff'/>");

        svg.append("<text x='").append(stageX + 20).append("' y='").append(stageY + 30)
                .append("' font-size='20' font-weight='700' fill='#0f172a'>Stage ")
                .append(escape(stage.path("alias").asText("N/A"))).append("</text>");

        svg.append("<text x='").append(stageX + 115).append("' y='").append(stageY + 30)
                .append("' font-size='11' fill='#475569'>")
                .append(escape(stage.path("id").asText(""))).append("</text>");

        svg.append("<text x='").append(stageX + stageWidth - 235).append("' y='").append(stageY + 30)
                .append("' font-size='11' fill='#475569'>Timeline: ")
                .append(stage.path("timeline").size()).append(" eventos</text>");

        appendGridAndAxes(svg, plotX, plotY, graphWidth, graphHeight, bounds);

        svg.append("<defs><clipPath id='").append(clipId).append("'>")
                .append("<rect x='").append(plotX).append("' y='").append(plotY)
                .append("' width='").append(graphWidth).append("' height='").append(graphHeight).append("'/>")
                .append("</clipPath></defs>");

        svg.append("<g clip-path='url(#").append(clipId).append(")'>");
        appendSegments(svg, stage.path("thermalProfile").path("segments"), contentXOffset, contentYOffset);
        appendTimeline(svg, stage.path("timeline"), contentXOffset, contentYOffset, plotY, plotBottomY, plotX, plotRightX);
        svg.append("</g>");

        svg.append("<rect x='").append(plotX).append("' y='").append(plotY)
                .append("' width='").append(graphWidth).append("' height='").append(graphHeight)
                .append("' fill='none' stroke='#94a3b8' stroke-width='1.2'/>");
    }

    private void appendGridAndAxes(StringBuilder svg, int plotX, int plotY, int graphWidth, int graphHeight, Bounds bounds) {
        int horizontalDivisions = 6;
        int verticalDivisions = 8;

        for (int i = 0; i <= horizontalDivisions; i++) {
            int y = plotY + (graphHeight * i / horizontalDivisions);
            svg.append("<line x1='").append(plotX).append("' y1='").append(y)
                    .append("' x2='").append(plotX + graphWidth).append("' y2='").append(y)
                    .append("' stroke='").append(i == horizontalDivisions ? "#94a3b8" : "#e2e8f0")
                    .append("' stroke-width='").append(i == horizontalDivisions ? "1.3" : "1")
                    .append("' stroke-dasharray='").append(i == horizontalDivisions ? "none" : "3,4")
                    .append("'/>");

            int coordinateValue = bounds.minY + ((bounds.maxY - bounds.minY) * i / horizontalDivisions);
            svg.append("<text x='").append(plotX - 12).append("' y='").append(y + 4)
                    .append("' text-anchor='end' font-size='10' fill='#64748b'>")
                    .append(coordinateValue).append("</text>");
        }

        for (int i = 0; i <= verticalDivisions; i++) {
            int x = plotX + (graphWidth * i / verticalDivisions);
            svg.append("<line x1='").append(x).append("' y1='").append(plotY)
                    .append("' x2='").append(x).append("' y2='").append(plotY + graphHeight)
                    .append("' stroke='").append(i == 0 ? "#94a3b8" : "#e2e8f0")
                    .append("' stroke-width='").append(i == 0 ? "1.3" : "1")
                    .append("' stroke-dasharray='").append(i == 0 ? "none" : "3,4")
                    .append("'/>");

            int coordinateValue = bounds.minX + ((bounds.maxX - bounds.minX) * i / verticalDivisions);
            svg.append("<text x='").append(x).append("' y='").append(plotY + graphHeight + 18)
                    .append("' text-anchor='middle' font-size='10' fill='#64748b'>")
                    .append(coordinateValue).append("</text>");
        }

        svg.append("<text x='").append(plotX - 38).append("' y='").append(plotY - 10)
                .append("' font-size='10' fill='#475569'>Y</text>");
        svg.append("<text x='").append(plotX + graphWidth + 12).append("' y='").append(plotY + graphHeight + 18)
                .append("' font-size='10' fill='#475569'>X</text>");
    }

    private void appendSegments(StringBuilder svg, JsonNode segments, int xOffset, int yOffset) {
        if (!segments.isArray()) {
            return;
        }

        for (JsonNode segment : segments) {
            int x1 = xOffset + segment.path("coordinates").path("start").path("x").asInt();
            int y1 = yOffset + segment.path("coordinates").path("start").path("y").asInt();
            int x2 = xOffset + segment.path("coordinates").path("end").path("x").asInt();
            int y2 = yOffset + segment.path("coordinates").path("end").path("y").asInt();

            svg.append("<line x1='").append(x1).append("' y1='").append(y1)
                    .append("' x2='").append(x2).append("' y2='").append(y2)
                    .append("' stroke='#2563eb' stroke-width='4' stroke-linecap='round'/>");

            svg.append("<circle cx='").append(x1).append("' cy='").append(y1).append("' r='4.5' fill='#2563eb'/>")
                    .append("<circle cx='").append(x2).append("' cy='").append(y2).append("' r='4.5' fill='#2563eb'/>");

            appendLineText(svg, x1, y1, x2, y2, buildSegmentLabel(segment), "#1e3a8a");
        }
    }

    private void appendTimeline(StringBuilder svg, JsonNode timeline, int xOffset, int yOffset,
                                int plotTopY, int plotBottomY, int plotLeftX, int plotRightX) {
        if (!timeline.isArray()) {
            return;
        }

        for (JsonNode event : timeline) {
            int x1 = clamp(xOffset + event.path("coordinates").path("start").path("x").asInt(), plotLeftX, plotRightX);
            int y1 = clamp(yOffset + event.path("coordinates").path("start").path("y").asInt(), plotTopY, plotBottomY);
            int x2 = clamp(xOffset + event.path("coordinates").path("end").path("x").asInt(), plotLeftX, plotRightX);
            int y2 = clamp(yOffset + event.path("coordinates").path("end").path("y").asInt(), plotTopY, plotBottomY);

            appendEventLine(svg, event, x1, y1, x2, y2);
        }
    }

    private void appendEventLine(StringBuilder svg, JsonNode event, int x1, int y1, int x2, int y2) {
        String color = switch (event.path("type").asText("")) {
            case "manual-dose-constant" -> "#16a34a";
            case "manual-dose-progressive" -> "#7c3aed";
            default -> "#dc2626";
        };

        svg.append("<line x1='").append(x1).append("' y1='").append(y1)
                .append("' x2='").append(x2).append("' y2='").append(y2)
                .append("' stroke='").append(color).append("' stroke-width='3' stroke-linecap='round'/>");
        svg.append("<circle cx='").append(x1).append("' cy='").append(y1).append("' r='4.5' fill='").append(color).append("'/>")
                .append("<circle cx='").append(x2).append("' cy='").append(y2).append("' r='4.5' fill='").append(color).append("'/>");

        appendLineText(svg, x1, y1, x2, y2, buildEventLabel(event), color);
        if (event.path("requiresConfirmation").asBoolean(false)) {
            svg.append("<text x='").append(x1 + 10).append("' y='").append(y1 + 24)
                    .append("' font-size='10' fill='#b45309'>Requiere confirmacion</text>");
        }
    }

    private void appendLineText(StringBuilder svg, int x1, int y1, int x2, int y2, String label, String color) {
        int labelX = Math.min(x1, x2) + 6;
        int labelY = Math.min(y1, y2) - 10;
        if (labelY < 18) {
            labelY = Math.max(y1, y2) + 16;
        }

        svg.append("<text x='").append(labelX).append("' y='").append(labelY)
                .append("' font-size='10' fill='").append(color).append("'>")
                .append(escape(truncate(label, 120)))
                .append("</text>");
    }

    private String buildSegmentLabel(JsonNode segment) {
        List<String> parts = new ArrayList<>();
        addTemperatureRange(parts, segment);
        addTextPart(parts, segment, "duration");
        addTextPart(parts, segment, "time");
        addTextPart(parts, segment, "rate");
        return parts.isEmpty() ? "Perfil termico" : String.join(" | ", parts);
    }

    private String buildEventLabel(JsonNode event) {
        List<String> parts = new ArrayList<>();
        if (event.path("labels").isArray()) {
            event.path("labels").forEach(label -> {
                if (label.isTextual() && !label.asText().isBlank()) {
                    parts.add(label.asText());
                }
            });
        }
        if (containsAny(event.path("type").asText(""), "drain", "drenaje")
                && parts.stream().noneMatch(part -> containsAny(part, "drain", "drenaje"))) {
            parts.add("Drenaje");
        }
        addTextPart(parts, event, "duration");
        addTextPart(parts, event, "time");
        addTextPart(parts, event, "flowRate");
        return String.join(" | ", parts);
    }

    private void addTemperatureRange(List<String> parts, JsonNode node) {
        String startTemperature = node.path("startTemperature").asText("");
        String endTemperature = node.path("endTemperature").asText("");
        if (!startTemperature.isBlank() || !endTemperature.isBlank()) {
            parts.add(startTemperature + "° -> " + endTemperature + "°");
        }
    }

    private void addTextPart(List<String> parts, JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
            parts.add(fieldName + ": " + value.asText());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private void appendLabelBox(StringBuilder svg, JsonNode labels, int x, int y, String background, String color) {
        List<String> items = new ArrayList<>();
        if (labels.isArray()) {
            for (JsonNode label : labels) {
                items.add(label.asText());
            }
        }
        if (items.isEmpty()) {
            return;
        }

        int boxHeight = Math.max(items.size() * 14 + 10, 24);
        int boxWidth = approximateLabelWidth(items);

        svg.append("<rect x='").append(x - 6).append("' y='").append(y - 12)
                .append("' width='").append(boxWidth).append("' height='").append(boxHeight)
                .append("' rx='8' ry='8' fill='").append(background).append("' fill-opacity='0.96' stroke='").append(color).append("' stroke-opacity='0.26'/>");

        int currentY = y;
        for (String item : items) {
            svg.append("<text x='").append(x).append("' y='").append(currentY)
                    .append("' font-size='11' fill='").append(color).append("'>")
                    .append(escape(item))
                    .append("</text>");
            currentY += 14;
        }
    }

    private int approximateLabelWidth(List<String> items) {
        int maxLength = items.stream().mapToInt(String::length).max().orElse(10);
        return Math.max(80, Math.min(220, maxLength * 7 + 18));
    }

    private Bounds calculateBounds(JsonNode stages) {
        Bounds bounds = new Bounds();
        if (!stages.isArray()) {
            return bounds.normalize();
        }

        for (JsonNode stage : stages) {
            collectBounds(stage.path("thermalProfile").path("segments"), bounds);
            collectBounds(stage.path("timeline"), bounds);
        }
        return bounds.normalize();
    }

    private void collectBounds(JsonNode items, Bounds bounds) {
        if (!items.isArray()) {
            return;
        }
        Iterator<JsonNode> iterator = items.iterator();
        while (iterator.hasNext()) {
            JsonNode item = iterator.next();
            bounds.include(item.path("coordinates").path("start").path("x").asInt(0),
                    item.path("coordinates").path("start").path("y").asInt(0));
            bounds.include(item.path("coordinates").path("end").path("x").asInt(0),
                    item.path("coordinates").path("end").path("y").asInt(0));
            if (item.has("triangleHeight")) {
                bounds.include(item.path("coordinates").path("end").path("x").asInt(0),
                        item.path("coordinates").path("end").path("y").asInt(0) - item.path("triangleHeight").asInt(0));
            }
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeId(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "-");
    }

    private record PdfPoint(double x, double y) {
    }

    private record PdfSegment(String stageAlias, String type, PdfPoint start, PdfPoint end,
                              String startTemperature, String endTemperature, String prevSegmentId) {
    }

    private record PdfTimelineEvent(String stageAlias, String type, PdfPoint start, PdfPoint end, String label,
                                    boolean requiresConfirmation, double originOffsetDy) {
    }

    private record PdfLine(float x1, float y1, float x2, float y2) {
    }

    private record PdfBounds(double minX, double minY, double maxX, double maxY) {
        static PdfBounds from(List<PdfSegment> segments, List<PdfTimelineEvent> timelineEvents) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            for (PdfSegment segment : segments) {
                minX = Math.min(minX, Math.min(segment.start().x(), segment.end().x()));
                minY = Math.min(minY, Math.min(segment.start().y(), segment.end().y()));
                maxX = Math.max(maxX, Math.max(segment.start().x(), segment.end().x()));
                maxY = Math.max(maxY, Math.max(segment.start().y(), segment.end().y()));
            }
            for (PdfTimelineEvent event : timelineEvents) {
                minX = Math.min(minX, Math.min(event.start().x(), event.end().x()));
                minY = Math.min(minY, Math.min(event.start().y(), event.end().y()));
                maxX = Math.max(maxX, Math.max(event.start().x(), event.end().x()));
                maxY = Math.max(maxY, Math.max(event.start().y(), event.end().y()));
            }

            double xPadding = Math.max((maxX - minX) * 0.05, 10);
            double yPadding = Math.max((maxY - minY) * 0.05, 10);
            return new PdfBounds(minX - xPadding, minY - yPadding, maxX + xPadding, maxY + yPadding);
        }

        double width() {
            return Math.max(maxX - minX, 1);
        }

        double height() {
            return Math.max(maxY - minY, 1);
        }
    }

    private static class Bounds {
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;

        void include(int x, int y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        Bounds normalize() {
            if (minX == Integer.MAX_VALUE) {
                minX = 0;
                minY = 0;
                maxX = 800;
                maxY = 400;
            }
            return this;
        }
    }

    private record ChartMetadata(
            String documentTitle,
            String documentSubtitle,
            String headerTitle,
            String headerSubtitle,
            String version
    ) {
        static ChartMetadata fromReceta(Receta receta) {
            String codReceta = receta.getCodReceta() == null ? "RECETA" : receta.getCodReceta();
            return new ChartMetadata(
                    "Curva de Diseno",
                    "Receta " + codReceta + " | ID " + receta.getId(),
                    codReceta,
                    "ID receta: " + receta.getId(),
                    null
            );
        }

        static ChartMetadata fromCurvaDiseno(CurvaDiseno curvaDiseno) {
            String descripcion = curvaDiseno.getDescripcion() == null || curvaDiseno.getDescripcion().isBlank()
                    ? "Curva de Diseno"
                    : curvaDiseno.getDescripcion();
            return new ChartMetadata(
                    "Curva de Diseno",
                    descripcion + " | ID " + curvaDiseno.getId(),
                    descripcion,
                    "ID curva: " + curvaDiseno.getId(),
                    curvaDiseno.getVersion()
            );
        }
    }
}
