# Stage build and compile
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Directory work
WORKDIR /app

#crate directorio para dependencias
RUN mkdir -p /root/.m2/repository/com/walrex/avro-schemas/0.0.1-SNAPSHOT

#Copy file pom.xml m
COPY ./pom.xml ./pom.xml
COPY ./module-core/pom.xml ./module-core/pom.xml
COPY ./module-users/pom.xml ./module-users/pom.xml
COPY ./module-role/pom.xml ./module-role/pom.xml
COPY ./gateway/pom.xml ./gateway/pom.xml
COPY ./module-mailing/pom.xml ./module-mailing/pom.xml

COPY ./m2-cache/com/walrex/avro-schemas/0.0.1-SNAPSHOT/* /root/.m2/repository/com/walrex/avro-schemas/0.0.1-SNAPSHOT

# Descargar dependencias (aprovechando caché de Docker)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY ./module-core/src ./module-core/src
COPY ./module-users/src ./module-users/src
COPY ./module-role/src ./module-role/src
COPY ./gateway/src ./gateway/src
COPY ./module-mailing/src ./module-mailing/src
COPY ./src ./src

# Compilar el proyecto completo (avro-schemas ya está disponible en .m2)
#RUN mvn clean package -DskipTests
RUN mvn clean install -DskipTests
#RUN cd module-core && mvn clean package -DskipTests

# Verificar archivos JAR generados
RUN ls -la module-core/target/*.jar

# Stage: make executable
FROM eclipse-temurin:21-jre-alpine AS runtime

# Directorio de trabajo
WORKDIR /app

# Instalar herramientas de monitorización y depuración
RUN apk add --no-cache curl jq netcat-openbsd

# Variables de entorno comunes
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV KAFKA_BOOTSTRAP_SERVER_0="kafka-1:9092"
ENV SCHEMA_REGISTRY_SERVER="schema-registry"
ENV SCHEMA_REGISTRY_PORT=8081
ENV DB_HOST="127.0.0.1"
ENV DB_PORT="5432"
ENV DB_NAME="erp_tlm_2021"
ENV DB_USER="postgres"
ENV DB_PASSWORD=12345
ENV SECRET_KEY_JWT="l7kP8lgYRt/PyIh/tBDYlg4QWCLf2RSOJ8oLPNV6O34="

# Crear usuario no privilegiado para ejecutar la aplicación
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app/logs

USER appuser

# Copiar JAR desde la etapa de construcción
COPY --from=build /app/module-core/target/*.jar /app/app.jar

# Script de inicio para verificar Kafka y otros servicios antes de arrancar
#COPY --chown=appuser:appgroup docker-entrypoint.sh /app/
#RUN chmod +x /app/docker-entrypoint.sh

# Exponer puertos
EXPOSE 8088

# Punto de entrada que verifica dependencias antes de arrancar
#ENTRYPOINT ["/app/docker-entrypoint.sh"]

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]