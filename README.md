# Monolito Modular - Walrex (V1)

Este proyecto es un Monolito Modular diseñado con **Arquitectura Hexagonal**, utilizando **Spring Modulith** para garantizar la separación de conceptos y la integridad de los dominios de negocio.

## Estructura del Proyecto

El sistema está dividido en múltiples módulos que representan áreas funcionales o capacidades técnicas específicas:

### Módulos Técnicos y Core
- **module-core**: Módulo principal que arranca la aplicación Spring Boot y orquesta la integración de todos los módulos.
- **module-common**: Utilidades, modelos compartidos y clases transversales.
- **module-security-commons**: Lógica compartida para la seguridad y gestión de tokens JWT.
- **module-websocket**: Servicio centralizado de notificaciones en tiempo real vía WebSockets.
- **gateway`: Puerta de enlace para el manejo de rutas y tráfico.

### Módulos de Negocio
- **module-almacen**: Gestión integral de almacenes, incluyendo ingresos, egresos, transformaciones de insumos, gestión de rollos y el nuevo sistema de **pesaje automático**.
- **module-articulos**: Gestión del catálogo de artículos y productos.
- **module-partidas**: Manejo de partidas de producción y seguimiento de procesos.
- **module-comercial**: Funcionalidades relacionadas con la gestión comercial y ventas.
- **module-users / module-role**: Gestión de identidad, usuarios y control de acceso basado en roles.
- **module-mailing**: Sistema de envío de correos electrónicos.
- **module-ecomprobantes**: Integración y generación de comprobantes electrónicos.
- **module-driver / module-liquidaciones**: Gestión de conductores y procesos de liquidación.
- **module-revision-tela**: Módulo especializado para la revisión de calidad de telas.

## Stack Tecnológico
- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Modulith**
- **R2DBC** (Acceso reactivo a base de datos PostgreSQL)
- **Apache Kafka** (Mensajería y captura de eventos con Avro)
- **Flyway** (Gestión de migraciones de base de datos)
- **Project Reactor** (Programación reactiva)
- **MapStruct** y **Lombok** (Productividad en el desarrollo)

## Cómo Ejecutar en Desarrollo

Para ejecutar la aplicación localmente utilizando el perfil de desarrollo y conectarse al entorno correspondiente, utilice el siguiente comando:

```bash
mvn clean install spring-boot:run -pl=module-core -Dspring-boot.run.profiles=dev -Dmaven.test.skip=true
```

## Características Recientes
- **Sistema de Pesaje**: Integración con balanzas digitales vía Raspberry Pi para el registro automático de peso en rollos de tela, con notificaciones en tiempo real al frontend.
- **Arquitectura Reactiva**: Implementación completa de stacks no bloqueantes para alta escalabilidad.
