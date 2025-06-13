# Stage build and compile
FROM maven:3.9.6-eclipse-temurin-21 AS build

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

RUN mkdir -p /root/.m2 && \
    printf '<settings>\n  <servers>\n    <server>\n      <id>github</id>\n      <username>%s</username>\n      <password>%s</password>\n    </server>\n  </servers>\n</settings>\n' "${GITHUB_USERNAME}" "${GITHUB_TOKEN}" > /root/.m2/settings.xml


# Directory work
WORKDIR /app

#Copy file pom.xml
COPY ./pom.xml ./pom.xml
COPY ./module-core/pom.xml ./module-core/pom.xml
COPY ./module-users/pom.xml ./module-users/pom.xml
COPY ./module-role/pom.xml ./module-role/pom.xml
COPY ./gateway/pom.xml ./gateway/pom.xml
COPY ./module-common/pom.xml ./module-common/pom.xml
COPY ./module-articulos/pom.xml ./module-articulos/pom.xml
COPY ./module-almacen/pom.xml ./module-almacen/pom.xml
COPY ./module-mailing/pom.xml ./module-mailing/pom.xml

# Descargar dependencias (aprovechando caché de Docker)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY ./module-common/src ./module-common/src
COPY ./module-core/src ./module-core/src
COPY ./module-users/src ./module-users/src
COPY ./module-role/src ./module-role/src
COPY ./gateway/src ./gateway/src
COPY ./module-articulos/src ./module-articulos/src
COPY ./module-almacen/src ./module-almacen/src
COPY ./module-mailing/src ./module-mailing/src
COPY ./src ./src

# Compilar el proyecto completo (avro-schemas ya está disponible en .m2)
#RUN mvn clean package -DskipTests
RUN mvn clean install -Dmaven.test.skip=true

# Verificar archivos JAR generados
RUN ls -la module-core/target/*.jar

# Stage: make executable
FROM eclipse-temurin:21-jre-alpine AS runtime

# Definir UID/GID específicos que coincidan con tu host
ARG USER_ID=1000
ARG GROUP_ID=1000

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
RUN addgroup -g ${GROUP_ID} -S appgroup && \
    adduser -u ${USER_ID} -S appuser -G appgroup && \
    mkdir -p /app/logs && \
    mkdir -p /app/traces && \
    chown -R appuser:appgroup /app/logs && \
    chown -R appuser:appgroup /app/traces

# Declarar volúmenes
VOLUME ["/app/logs", "/app/traces"]

USER appuser

# Copiar JAR desde la etapa de construcción
COPY --from=build /app/module-core/target/*.jar /app/app.jar

# Exponer puertos
EXPOSE 8088

# Comando para ejecutar la aplicación
CMD ["java", "-Dreactor.tools.agent.enabled=false", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]