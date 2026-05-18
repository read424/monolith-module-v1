package com.walrex.kafka.aop.serialization;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 📦 Helper para serialización/deserialización Avro automática
 * 
 * Funcionalidades:
 * - Configuración automática de Schema Registry
 * - Deserialización dinámica basada en clase
 * - Manejo de versiones de schema
 * - Cache de configuraciones
 * - Estrategias de naming
 * 
 * @author Kafka AOP Module
 */
@Component
@Slf4j
public class AvroSerializationHelper {

    @Value("${spring.kafka.producer.properties.schema.registry.url:http://localhost:8081}")
    private String defaultSchemaRegistryUrl;

    private final Map<String, Map<String, Object>> configCache = new HashMap<>();

    /**
     * 🔧 Obtiene configuración de Schema Registry
     */
    public Map<String, Object> getSchemaRegistryConfig(String schemaRegistryUrl) {
        String resolvedUrl = resolveSchemaRegistryUrl(schemaRegistryUrl);
        
        return configCache.computeIfAbsent(resolvedUrl, url -> {
            Map<String, Object> config = new HashMap<>();
            
            // ✅ Configuración básica de Schema Registry
            config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, url);
            config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
            config.put(KafkaAvroDeserializerConfig.USE_LATEST_VERSION, true);
            config.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, true);
            
            // ✅ Estrategia de naming para topics
            config.put(KafkaAvroDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicNameStrategy.class.getName());
            
            // ✅ Configuración de compatibilidad
            config.put("schema.registry.basic.auth.credentials.source", "USER_INFO");
            config.put("schema.registry.basic.auth.user.info", "");
            
            log.info("✅ Configuración Schema Registry creada para URL: {}", url);
            return config;
        });
    }

    /**
     * 🔄 Deserializa objeto Avro automáticamente
     */
    public Object deserialize(Object value, Class<?> targetClass) {
        try {
            if (value == null) {
                log.warn("⚠️ Valor nulo recibido para deserialización");
                return null;
            }

            // ✅ Si ya es del tipo correcto, no hacer nada
            if (targetClass.isInstance(value)) {
                log.debug("✅ Valor ya es del tipo correcto: {}", targetClass.getSimpleName());
                return value;
            }

            // ✅ Si es un GenericRecord, intentar conversión
            if (value instanceof org.apache.avro.generic.GenericRecord genericRecord) {
                return deserializeFromGenericRecord(genericRecord, targetClass);
            }

            // ✅ Si es SpecificRecord, intentar cast directo
            if (value instanceof org.apache.avro.specific.SpecificRecord) {
                if (targetClass.isInstance(value)) {
                    return targetClass.cast(value);
                } else {
                    log.warn("⚠️ SpecificRecord no es del tipo esperado. Esperado: {}, Actual: {}", 
                        targetClass.getSimpleName(), value.getClass().getSimpleName());
                    return value; // Devolver tal como está
                }
            }

            // ✅ Para otros tipos, intentar conversión directa
            log.debug("🔄 Intentando conversión directa de {} a {}", 
                value.getClass().getSimpleName(), targetClass.getSimpleName());
            return value;

        } catch (Exception e) {
            log.error("❌ Error deserializando a {}: {}", targetClass.getSimpleName(), e.getMessage());
            throw new AvroSerializationException("Error deserializando objeto Avro", e);
        }
    }

    /**
     * 📄 Deserializa desde GenericRecord a clase específica
     */
    private Object deserializeFromGenericRecord(org.apache.avro.generic.GenericRecord genericRecord, Class<?> targetClass) {
        try {
            // ✅ Verificar si la clase target tiene constructor desde GenericRecord
            if (hasGenericRecordConstructor(targetClass)) {
                return targetClass.getConstructor(org.apache.avro.generic.GenericRecord.class)
                    .newInstance(genericRecord);
            }

            // ✅ Intentar usar reflection para mapear campos
            return mapGenericRecordToObject(genericRecord, targetClass);

        } catch (Exception e) {
            log.error("❌ Error deserializando GenericRecord a {}: {}", targetClass.getSimpleName(), e.getMessage());
            throw new AvroSerializationException("Error convertir GenericRecord", e);
        }
    }

    /**
     * 🔍 Verifica si la clase tiene constructor que acepta GenericRecord
     */
    private boolean hasGenericRecordConstructor(Class<?> targetClass) {
        try {
            targetClass.getConstructor(org.apache.avro.generic.GenericRecord.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * 🗺️ Mapea GenericRecord a objeto usando reflection
     */
    private Object mapGenericRecordToObject(org.apache.avro.generic.GenericRecord genericRecord, Class<?> targetClass) {
        // TODO: Implementar mapeo automático usando reflection
        // Por ahora, devolver el GenericRecord tal como está
        log.warn("⚠️ Mapeo automático no implementado. Devolviendo GenericRecord para {}", targetClass.getSimpleName());
        return genericRecord;
    }

    /**
     * 🔄 Serializa objeto a Avro automáticamente
     */
    public Object serialize(Object value, Class<?> schemaClass) {
        try {
            if (value == null) {
                return null;
            }

            // ✅ Si ya es del tipo de schema correcto, no hacer nada
            if (schemaClass.isInstance(value)) {
                log.debug("✅ Valor ya es del tipo de schema correcto: {}", schemaClass.getSimpleName());
                return value;
            }

            // ✅ TODO: Implementar conversión automática a schema Avro
            log.debug("🔄 Serialización automática no implementada. Devolviendo valor original");
            return value;

        } catch (Exception e) {
            log.error("❌ Error serializando a schema {}: {}", schemaClass.getSimpleName(), e.getMessage());
            throw new AvroSerializationException("Error serializando objeto a Avro", e);
        }
    }

    /**
     * 🔗 Resuelve URL de Schema Registry (soporte para placeholders)
     */
    private String resolveSchemaRegistryUrl(String schemaRegistryUrl) {
        if (schemaRegistryUrl == null || schemaRegistryUrl.trim().isEmpty()) {
            return defaultSchemaRegistryUrl;
        }

        // ✅ Resolver placeholders básicos
        if (schemaRegistryUrl.startsWith("${") && schemaRegistryUrl.endsWith("}")) {
            // Para este caso simple, usar la URL por defecto
            return defaultSchemaRegistryUrl;
        }

        return schemaRegistryUrl;
    }

    /**
     * 📋 Obtiene información del schema
     */
    public SchemaInfo getSchemaInfo(Class<?> schemaClass) {
        try {
            // ✅ Intentar obtener información del schema desde la clase
            String schemaName = schemaClass.getSimpleName();
            String packageName = schemaClass.getPackageName();
            
            // ✅ TODO: Integrar con Schema Registry para obtener versión real
            int version = 1;
            
            return new SchemaInfo(schemaName, packageName, version);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo información del schema para {}: {}", 
                schemaClass.getSimpleName(), e.getMessage());
            return new SchemaInfo("unknown", "unknown", 0);
        }
    }

    /**
     * 🧹 Limpia cache de configuraciones
     */
    public void clearCache() {
        configCache.clear();
        log.info("🧹 Cache de configuraciones Schema Registry limpiado");
    }

    /**
     * 📊 Record para información del schema
     */
    public record SchemaInfo(
        String name,
        String packageName,
        int version
    ) {}

    /**
     * ❌ Excepción personalizada para errores de serialización
     */
    public static class AvroSerializationException extends RuntimeException {
        public AvroSerializationException(String message) {
            super(message);
        }
        
        public AvroSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 