# 🔄 Patrón Request/DTO/Response - Motivos de Devolución

## 📋 Resumen

Se ha implementado el patrón **Request/DTO/Response** separando claramente los modelos de entrada de la API, el dominio interno y las respuestas de salida. Este patrón mejora la seguridad, mantenibilidad y flexibilidad del sistema.

## 🎯 Arquitectura Implementada

```
📁 Flujo de Datos:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Request   │ -> │  Domain DTO     │ -> │  API Response   │
│ (CrearRequest)  │    │ (MotivosDTO)    │    │ (MotivosResp)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         |                        |                        |
         v                        v                        v
  🔍 Validaciones          🏗️ Lógica              📤 Datos
  🚪 Entrada API           💼 Dominio             🔒 Seguros
```

## 🏗️ Estructura de Archivos

```
📁 infrastructure/adapters/inbound/reactiveweb/
├── request/
│   └── CrearMotivoDevolucionRequest.java       # ✨ NUEVO - Request de entrada
├── response/
│   └── MotivoDevolucionResponse.java           # Response de salida
├── mapper/
│   ├── CrearMotivoDevolucionRequestMapper.java # ✨ NUEVO - Request -> DTO
│   └── MotivoDevolucionResponseMapper.java     # DTO -> Response
└── MotivosDevolucionHandler.java               # 🔄 ACTUALIZADO

📁 domain/model/dto/
└── MotivoDevolucionDTO.java                    # DTO del dominio (interno)
```

## 🔧 Implementación Detallada

### 1. **CrearMotivoDevolucionRequest** (Entrada API)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CrearMotivoDevolucionRequest", description = "Request para crear un nuevo motivo de devolución")
public class CrearMotivoDevolucionRequest {

    @NotNull(message = "Campo descripción es obligatorio")
    @Schema(
        description = "Descripción del motivo de devolución",
        example = "DEFECTO DE FABRICACIÓN",
        required = true,
        maxLength = 255
    )
    private String descripcion;
}
```

**Características:**

- ✅ **Validaciones de entrada**: `@NotNull`
- ✅ **Solo campos necesarios**: Solo `descripcion`
- ✅ **Documentación Swagger**: Ejemplos y descripciones
- ✅ **Validación Bean Validation**: Automática con Spring

### 2. **MotivoDevolucionDTO** (Dominio Interno)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDevolucionDTO {
    private Long id;                    // Generado por BD
    private String descripcion;         // Del request
    private Integer status;             // Asignado por mapper (1=activo)
    private OffsetDateTime createAt;    // Generado por BD
    private OffsetDateTime updateAt;    // Generado por BD
}
```

**Características:**

- ✅ **Modelo completo**: Todos los campos del dominio
- ✅ **Sin anotaciones de API**: Puro dominio
- ✅ **Campos de auditoría**: `createAt`, `updateAt`

### 3. **MotivoDevolucionResponse** (Salida API)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MotivoDevolucion", description = "Modelo de datos para motivos de devolución")
public class MotivoDevolucionResponse {

    @Schema(description = "Identificador único del motivo", example = "1")
    private Long id;

    @Schema(description = "Descripción del motivo", example = "DEFECTO DE FABRICACIÓN")
    private String descripcion;

    @Schema(description = "Estado del motivo (1=activo, 0=inactivo)", example = "1")
    private Integer status;

    // ❌ NO incluye createAt ni updateAt (campos internos)
}
```

**Características:**

- ✅ **Solo campos públicos**: Excluye campos internos
- ✅ **Documentación completa**: Para Swagger
- ✅ **Datos seguros**: Sin información sensible

## 🔄 Mappers Implementados

### 1. **CrearMotivoDevolucionRequestMapper** (Request → DTO)

```java
@Mapper(componentModel = "spring")
public interface CrearMotivoDevolucionRequestMapper {

    @Mapping(target = "id", ignore = true)                          // Será asignado por la BD
    @Mapping(target = "descripcion", source = "descripcion")        // Mapeo directo
    @Mapping(target = "status", constant = "1")                     // Activo por defecto
    @Mapping(target = "createAt", ignore = true)                    // Será asignado por la BD
    @Mapping(target = "updateAt", ignore = true)                    // Será asignado por la BD
    MotivoDevolucionDTO toDTO(CrearMotivoDevolucionRequest request);
}
```

### 2. **MotivoDevolucionResponseMapper** (DTO → Response)

```java
@Mapper(componentModel = "spring")
public interface MotivoDevolucionResponseMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "status", source = "status")
    MotivoDevolucionResponse toResponse(MotivoDevolucionDTO motivoDevolucionDTO);

    List<MotivoDevolucionResponse> toResponseList(List<MotivoDevolucionDTO> motivosDevolucionDTO);
}
```

## 🎯 Handler Actualizado

### Flujo Completo:

```java
@Component
@RequiredArgsConstructor
public class MotivosDevolucionHandler {

    private final GestionarMotivosDevolucionUseCase gestionarMotivosDevolucionUseCase;
    private final MotivoDevolucionResponseMapper responseMapper;
    private final CrearMotivoDevolucionRequestMapper requestMapper;  // ✨ NUEVO

    public Mono<ServerResponse> crearMotivoDevolucion(ServerRequest request) {
        return request.bodyToMono(CrearMotivoDevolucionRequest.class)    // 1️⃣ Recibir Request
                .map(requestMapper::toDTO)                               // 2️⃣ Request → DTO
                .flatMap(gestionarMotivosDevolucionUseCase::crearMotivoDevolucion)  // 3️⃣ Lógica negocio
                .map(responseMapper::toResponse)                         // 4️⃣ DTO → Response
                .flatMap(motivoCreado -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(motivoCreado));                       // 5️⃣ Devolver Response
    }
}
```

## 📊 Comparación de Modelos

| Campo         | Request | DTO | Response | Notas                        |
| ------------- | ------- | --- | -------- | ---------------------------- |
| `id`          | ❌      | ✅  | ✅       | Generado por BD              |
| `descripcion` | ✅      | ✅  | ✅       | Campo principal              |
| `status`      | ❌      | ✅  | ✅       | Asignado automáticamente (1) |
| `createAt`    | ❌      | ✅  | ❌       | Campo interno de auditoría   |
| `updateAt`    | ❌      | ✅  | ❌       | Campo interno de auditoría   |

## 📝 Ejemplos de Uso

### Request de Creación:

```json
POST /almacen/motivos-devolucion
Content-Type: application/json

{
  "descripcion": "DEFECTO DE FABRICACIÓN"
}
```

### Response de Creación:

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "descripcion": "DEFECTO DE FABRICACIÓN",
  "status": 1
}
```

### Response de Lista:

```json
GET /almacen/motivos-devolucion

[
  {
    "id": 1,
    "descripcion": "DEFECTO DE FABRICACIÓN",
    "status": 1
  },
  {
    "id": 2,
    "descripcion": "DAÑO EN TRANSPORTE",
    "status": 1
  }
]
```

## ✅ Beneficios Implementados

### 1. **Seguridad**

- ❌ No expone campos internos (`createAt`, `updateAt`)
- ✅ Control total sobre entrada y salida de datos
- ✅ Validaciones específicas por endpoint

### 2. **Flexibilidad**

- ✅ Diferentes requests para diferentes operaciones
- ✅ Diferentes responses según el contexto
- ✅ Mappers pueden transformar/formatear datos

### 3. **Mantenibilidad**

- ✅ Cambios en API no afectan el dominio
- ✅ Cambios en dominio no afectan la API
- ✅ Separación clara de responsabilidades

### 4. **Documentación**

- ✅ Swagger específico para cada modelo
- ✅ Ejemplos claros y validaciones documentadas
- ✅ Fácil comprensión para consumers de API

### 5. **Validación**

- ✅ Bean Validation en requests
- ✅ Validaciones de negocio en DTOs
- ✅ Control de datos en responses

## 🔧 Configuración MapStruct

### Dependencias (ya incluidas):

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.3.Final</version>
</dependency>
```

### Características de los Mappers:

- `componentModel = "spring"`: Integración con Spring DI
- `@Mapping`: Control específico de cada campo
- `ignore = true`: Campos no mapeados
- `constant = "valor"`: Valores por defecto
- `source = "campo"`: Mapeo directo

## 🎯 Próximas Extensiones

1. **Más Requests**: `ActualizarMotivoDevolucionRequest`
2. **Validaciones Avanzadas**: `@Pattern`, `@Size`, etc.
3. **Responses Paginados**: Con metadata de paginación
4. **Transformaciones**: Formateo de fechas, normalización
5. **Versionado**: Diferentes versiones de requests/responses

---

✅ **Implementación completa del patrón Request/DTO/Response siguiendo arquitectura hexagonal y principios SOLID**
