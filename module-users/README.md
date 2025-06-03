# Module Users

Módulo de gestión de usuarios para el monolito modular, construido con Spring Boot.

## Funcionalidades
- CRUD de usuarios
- Registro y autenticación
- Gestión de perfiles de usuario
- Validación de datos
- Integración con module-role
- APIs REST para gestión de usuarios

## Tecnologías
- Spring Boot
- Spring Security
- Spring Data JPA/R2DBC
- PostgreSQL/MySQL
- Reactor (WebFlux)
- Validation

## Uso en el monolito
Este módulo se integra como subtree y proporciona servicios de gestión de usuarios.

## Dependencias
- module-common (DTOs y utilities)
- module-role (para asignación de roles)
- module-core (configuración base)

## Entidades principales
- User
- UserProfile
- UserCredentials
