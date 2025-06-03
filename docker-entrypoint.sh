#!/bin/sh
set -e

# Función para verificar la disponibilidad de un servicio
check_service() {
    SERVICE_NAME=$1
    HOST=$2
    PORT=$3
    MAX_RETRY=30
    RETRY_INTERVAL=2

    echo "⏳ Verificando conexión a $SERVICE_NAME ($HOST:$PORT)..."

    RETRY_COUNT=0
    until nc -z $HOST $PORT > /dev/null 2>&1; do
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -eq $MAX_RETRY ]; then
            echo "❌ No se pudo conectar a $SERVICE_NAME después de $MAX_RETRY intentos"
            return 1
        fi

        echo "⏳ Intentando conectar a $SERVICE_NAME (intento $RETRY_COUNT/$MAX_RETRY)..."
        sleep $RETRY_INTERVAL
    done

    echo "✅ Conexión establecida con $SERVICE_NAME"
    return 0
}

# Verificar conexión con Kafka
KAFKA_HOST=${KAFKA_HOST:-kafka}
KAFKA_PORT=${KAFKA_PORT:-9092}
check_service "Kafka" $KAFKA_HOST $KAFKA_PORT

# Verificar conexión con Schema Registry
SCHEMA_REGISTRY_HOST=${SCHEMA_REGISTRY_HOST:-schema-registry}
SCHEMA_REGISTRY_PORT=${SCHEMA_REGISTRY_PORT:-8081}
check_service "Schema Registry" $SCHEMA_REGISTRY_HOST $SCHEMA_REGISTRY_PORT

# Verificar conexión con PostgreSQL
POSTGRES_HOST=${POSTGRES_HOST:-postgres}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
check_service "PostgreSQL" $POSTGRES_HOST $POSTGRES_PORT

# Imprimir información de configuración
echo "🔧 Configuración:"
echo "- Perfil activo: $SPRING_PROFILES_ACTIVE"
echo "- Opciones de JVM: $JAVA_OPTS"

# Ejecutar comando principal (java -jar app.jar)
echo "🚀 Iniciando aplicación..."
exec "$@"