package com.walrex.module_almacen.common.utils;

import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RowMapperHelper {

    public <T> T mapRow(Row row, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();

            Arrays.stream(targetClass.getDeclaredFields())
                    .forEach(field -> {
                        try {
                            field.setAccessible(true);
                            String columnName = camelToSnake(field.getName());
                            Object value = row.get(columnName, field.getType());
                            field.set(instance, value);
                        } catch (Exception ignored) {
                            // Campo no encontrado en row
                        }
                    });

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando row", e);
        }
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
