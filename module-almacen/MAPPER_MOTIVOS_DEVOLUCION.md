# 🔄 Mapper MotivoDevolucion - Separación DTO/Response

## 📋 Resumen

Se ha implementado un mapper usando **MapStruct** para convertir entre el DTO del dominio (`MotivoDevolucionDTO`) y el response de la API (`MotivoDevolucionResponse`), siguiendo el principio de separación de responsabilidades.

## 🎯 Arquitectura

```
📁 domain/model/dto/
└── MotivoDevolucionDTO.java          # DTO del dominio (completo)

📁 infrastructure/adapters/inbound/reactiveweb/
├── response/
│   └── MotivoDevolucionResponse.java  # Response de API (simplificado)
├── mapper/
│   └── MotivoDevolucionResponseMapper.java  # Mapper MapStruct
└── MotivosDevolucionHandler.java      # Handler actualizado
```

## 🔧 Implementación

### 1. **MotivoDevolucionDTO** (Dominio)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDevolucionDTO {
    private Long id;
    private String descripcion;
    private Integer status;
    private OffsetDateTime createAt;    // ✅ Campo interno
    private OffsetDateTime updateAt;    // ✅ Campo interno
}
```

### 2. **MotivoDevolucionResponse** (API)

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

### 3. **MotivoDevolucionResponseMapper** (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface MotivoDevolucionResponseMapper {

    MotivoDevolucionResponseMapper INSTANCE = Mappers.getMapper(MotivoDevolucionResponseMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "status", source = "status")
    MotivoDevolucionResponse toResponse(MotivoDevolucionDTO motivoDevolucionDTO);

    List<MotivoDevolucionResponse> toResponseList(List<MotivoDevolucionDTO> motivosDevolucionDTO);
}
```

## 🔄 Uso en el Handler

### Antes (usando DTO directamente):

```java
@Component
@RequiredArgsConstructor
public class MotivosDevolucionHandler {
    private final GestionarMotivosDevolucionUseCase useCase;

    public Mono<ServerResponse> obtenerMotivosActivos(ServerRequest request) {
        return useCase.obtenerMotivosActivos()
                .collectList()
                .flatMap(motivos -> ServerResponse.ok()
                        .bodyValue(motivos));  // ❌ Expone campos internos
    }
}
```

### Después (usando Mapper):

```java
@Component
@RequiredArgsConstructor
public class MotivosDevolucionHandler {
    private final GestionarMotivosDevolucionUseCase useCase;
    private final MotivoDevolucionResponseMapper responseMapper;  // ✅ Mapper inyectado

    public Mono<ServerResponse> obtenerMotivosActivos(ServerRequest request) {
        return useCase.obtenerMotivosActivos()
                .collectList()
                .map(responseMapper::toResponseList)  // ✅ Transformación con mapper
                .flatMap(motivos -> ServerResponse.ok()
                        .bodyValue(motivos));
    }
}
```

## 📊 Comparación de Respuestas

### Antes (DTO completo):

```json
{
  "id": 1,
  "descripcion": "DEFECTO DE FABRICACIÓN",
  "status": 1,
  "createAt": "2024-01-15T10:30:00Z", // ❌ Campo interno expuesto
  "updateAt": "2024-01-15T10:30:00Z" // ❌ Campo interno expuesto
}
```

### Después (Response limpio):

```json
{
  "id": 1,
  "descripcion": "DEFECTO DE FABRICACIÓN",
  "status": 1
}
```

## ✅ Beneficios Implementados

### 1. **Separación de Responsabilidades**

- **DTO**: Modelo de dominio con todos los campos
- **Response**: Modelo de API con campos específicos para exposición

### 2. **Seguridad**

- ❌ No expone campos internos como `createAt` y `updateAt`
- ✅ Control total sobre qué campos se devuelven al cliente

### 3. **Flexibilidad**

- ✅ Mapper puede transformar/formatear datos sin afectar el dominio
- ✅ Fácil agregar campos calculados o transformaciones

### 4. **Documentación Swagger**

- ✅ Response documentado específicamente para la API
- ✅ Ejemplos y validaciones específicas para cada endpoint

### 5. **Mantenibilidad**

- ✅ Cambios en el dominio no afectan la API
- ✅ Cambios en la API no afectan el dominio
- ✅ Mapper centralizado para transformaciones

## 🚀 Métodos Disponibles

### Conversión Individual:

```java
MotivoDevolucionResponse response = responseMapper.toResponse(motivoDTO);
```

### Conversión de Lista:

```java
List<MotivoDevolucionResponse> responses = responseMapper.toResponseList(motivosDTO);
```

## 🔧 Configuración MapStruct

### Dependencia (ya incluida en pom.xml):

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.3.Final</version>
</dependency>
```

### Configuración del Mapper:

- `componentModel = "spring"`: Integración con Spring DI
- `@Mapping`: Mapeo explícito de campos
- `INSTANCE`: Patrón Singleton para uso directo

## 📋 Endpoints Actualizados

| Endpoint                             | Método | Response                         |
| ------------------------------------ | ------ | -------------------------------- |
| `/almacen/motivos-devolucion`        | GET    | `List<MotivoDevolucionResponse>` |
| `/almacen/motivos-devolucion/buscar` | GET    | `List<MotivoDevolucionResponse>` |
| `/almacen/motivos-devolucion`        | POST   | `MotivoDevolucionResponse`       |

## 🎯 Siguientes Pasos

1. **Extensión**: Agregar más campos al response si es necesario
2. **Validación**: Validaciones específicas en el response
3. **Transformaciones**: Formateo de datos (fechas, números, etc.)
4. **Paginación**: Response con metadata de paginación
5. **Versionado**: Diferentes responses para diferentes versiones de API

---

✅ **Implementación completa siguiendo arquitectura hexagonal, principios SOLID y patrones de Clean Code**
