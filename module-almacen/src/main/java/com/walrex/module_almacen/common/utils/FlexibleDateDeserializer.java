package com.walrex.module_almacen.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class FlexibleDateDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),     // 2025-06-05T05:00:00.000Z
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),         // 2025-06-05T05:00:00Z
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"),            // 2025-06-05T05:00Z
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),        // 2025-06-05T05:00:00.000
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),            // 2025-06-05T05:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),               // 2025-06-05T05:00
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                           // ISO estándar
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),              // 2025-06-05 05:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")                  // 2025-06-05 05:00
    );

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getValueAsString();

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        // Limpiar la cadena
        dateString = dateString.trim();

        // Intentar con cada formato
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // Si termina en Z, removerla para LocalDateTime
                String cleanDateString = dateString.endsWith("Z") ?
                        dateString.substring(0, dateString.length() - 1) : dateString;

                return LocalDateTime.parse(cleanDateString, formatter);
            } catch (DateTimeParseException e) {
                // Continuar con el siguiente formato
            }
        }

        // Si ningún formato funciona, lanzar excepción descriptiva
        throw new IOException(String.format(
                "No se pudo parsear la fecha '%s'. Formatos soportados: %s",
                dateString,
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z', yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd'T'HH:mm, etc."
        ));
    }
}
