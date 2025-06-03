# Module Role

Módulo de gestión de roles y permisos para el monolito modular, construido con Spring Boot.

## Funcionalidades
- CRUD de roles y permisos
- Asignación de roles a usuarios
- Control de acceso basado en roles (RBAC)
- Jerarquía de permisos
- Validación de autorización
- Audit trail de cambios

## Tecnologías
- Spring Boot
- Spring Security
- Spring Data JPA/R2DBC
- PostgreSQL/MySQL
- Reactor (WebFlux)

## Uso en el monolito
Este módulo se integra como subtree y proporciona servicios de autorización para otros módulos.

## Entidades principales
- Role
- Permission
- RolePermission
- UserRole
