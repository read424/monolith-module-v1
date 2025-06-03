# Module Core

Módulo core con las funcionalidades centrales del sistema para el monolito modular.

## Funcionalidades
- Configuración central del sistema
- Servicios base y abstracciones
- Seguridad y autenticación base
- Manejo de transacciones
- Configuración de base de datos
- Logging y monitoring

## Tecnologías
- Spring Boot
- Spring Security
- Spring Data
- PostgreSQL/MySQL
- Reactor (WebFlux)
- Observabilidad con Prometheus

## Uso en el monolito
Este módulo se integra como subtree y proporciona funcionalidades base para otros módulos.

## Dependencias
Otros módulos que pueden depender de module-core:
- gateway
- module-users
- module-role
- module-articulos
- etc.
