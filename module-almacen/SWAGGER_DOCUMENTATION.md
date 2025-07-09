# 📚 Documentación Swagger - Motivos de Devolución

## 🚀 Acceso a la Documentación

### URLs de Acceso:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/document`

### ⚙️ Configuración

La documentación está configurada para:

- **Ruta pública**: Sin validación JWT
- **Endpoint público**: `/document` (sin prefijo `/api/v2`)
- **Grupo específico**: `motivos-devolucion`

## 📋 Endpoints Documentados

### 1. 🔍 Obtener Motivos Activos

```
GET /almacen/motivos-devolucion
```

**Descripción**: Obtiene todos los motivos de devolución activos
**Respuesta**: Lista de motivos con ID, descripción y estado

### 2. 🔎 Buscar Motivos por Descripción

```
GET /almacen/motivos-devolucion/buscar?q=texto
```

**Descripción**: Busca motivos que contengan el texto especificado
**Parámetros**:

- `q` (query): Texto a buscar (opcional)

### 3. ➕ Crear Nuevo Motivo

```
POST /almacen/motivos-devolucion
Content-Type: application/json

{
  "descripcion": "DEFECTO DE FABRICACIÓN",
  "status": 1
}
```

**Descripción**: Crea un nuevo motivo de devolución
**Validaciones**:

- Descripción obligatoria (máx 255 caracteres)
- Descripción debe ser única
- Status por defecto: 1 (activo)

## 🔧 Configuración Técnica

### Dependencias

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-webflux-ui</artifactId>
    <version>1.8.0</version>
</dependency>
```

### Configuración application.yml

```yaml
spring:
  main:
    web-application-type: reactive

springdoc:
  api-docs:
    path: /document
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  group-configs:
    - group: motivos-devolucion
      display-name: "Motivos de Devolución"
      paths-to-match: /almacen/motivos-devolucion/**
```

## 📊 Modelos de Datos

### MotivoDevolucionDTO

```json
{
  "id": 1,
  "descripcion": "DEFECTO DE FABRICACIÓN",
  "status": 1,
  "createAt": "2024-01-15T10:30:00Z",
  "updateAt": "2024-01-15T10:30:00Z"
}
```

**Campos**:

- `id`: Long (solo lectura) - Identificador único
- `descripcion`: String (requerido) - Descripción del motivo (máx 255 caracteres)
- `status`: Integer - Estado (1=activo, 0=inactivo)
- `createAt`: OffsetDateTime (solo lectura) - Fecha de creación
- `updateAt`: OffsetDateTime (solo lectura) - Fecha de actualización

## 🛠️ Arquitectura

La documentación sigue la **arquitectura hexagonal** del proyecto:

```
📁 infrastructure/config/
├── OpenApiConfig.java           # Configuración OpenAPI/Swagger
└── router/
    └── RouterMotivosDevolucionReactiveAPI.java  # Documentación de rutas

📁 infrastructure/adapters/inbound/reactiveweb/
└── MotivosDevolucionHandler.java  # Handlers con anotaciones OpenAPI

📁 domain/model/dto/
└── MotivoDevolucionDTO.java      # DTO con anotaciones Schema
```

## 📱 Ejemplos de Uso

### Obtener todos los motivos activos:

```bash
curl -X GET "http://localhost:8080/almacen/motivos-devolucion" \
  -H "accept: application/json"
```

### Buscar motivos:

```bash
curl -X GET "http://localhost:8080/almacen/motivos-devolucion/buscar?q=defecto" \
  -H "accept: application/json"
```

### Crear nuevo motivo:

```bash
curl -X POST "http://localhost:8080/almacen/motivos-devolucion" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "DAÑO EN TRANSPORTE",
    "status": 1
  }'
```

## 🔐 Seguridad

- Los endpoints están configurados como **rutas públicas**
- **No requieren autenticación JWT**
- La ruta `/document` está excluida de validación JWT

## 📈 Funcionalidades Avanzadas

### Validaciones Documentadas:

- ✅ Descripción obligatoria
- ✅ Longitud máxima (255 caracteres)
- ✅ Unicidad de descripción
- ✅ Normalización automática (UPPERCASE)

### Respuestas de Error:

- `400 Bad Request`: Datos inválidos o descripción duplicada
- Mensajes de error descriptivos en español

### Patrones Reactivos:

- Compatible con **WebFlux**
- Usa **Mono** y **Flux** para operaciones reactivas
- Documentación específica para **RouterFunction + Handler**

---

🎯 **Nota**: Esta documentación está optimizada para el módulo de almacén y sigue las reglas arquitectónicas del proyecto (hexagonal, SOLID, clean code).
