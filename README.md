# 📦 Módulo Almacén

Módulo de gestión de inventario y movimientos de almacén desarrollado con Spring Boot y arquitectura hexagonal (Ports & Adapters).

## 🏗️ Arquitectura

Este módulo sigue los principios de **Arquitectura Hexagonal** y **Domain-Driven Design (DDD)**:

```
module-almacen/
├── 📁 domain/                    # Núcleo del negocio
│   ├── model/                    # Entidades de dominio
│   ├── exceptions/               # Excepciones del dominio
│   └── enums/                    # Enumeraciones
├── 📁 application/               # Casos de uso
│   ├── ports/
│   │   ├── input/               # Puertos de entrada (Use Cases)
│   │   └── output/              # Puertos de salida (Repositories)
│   └── services/                # Implementación de casos de uso
└── 📁 infrastructure/           # Adaptadores
    ├── adapters/
    │   ├── inbound/            # Controladores/Handlers
    │   └── outbound/           # Repositorios/Persistencia
    └── config/                 # Configuraciones
```

## 🚀 Características Principales

### ✅ Gestión de Órdenes de Ingreso
- **Ingreso Logístico General**: Productos estándar
- **Ingreso de Tela Cruda**: Con manejo de rollos específicos
- **Transformación de Insumos**: Proceso de ingreso + salida atómica

### ✅ Patrón Factory
- Selección dinámica de adaptadores según tipo de operación
- Extensible para nuevos tipos de órdenes
- Configuración mediante `@Qualifier`

### ✅ Gestión de Kardex
- Registro automático de movimientos de inventario
- Cálculo de saldos y conversiones de unidades
- Estrategias específicas por tipo de operación

### ✅ Validaciones Robustas
- Bean Validation en DTOs de entrada
- Validaciones de negocio en servicios
- Manejo consistente de errores

## 🛠️ Stack Tecnológico

- **Framework**: Spring Boot 3.x
- **Programación Reactiva**: Spring WebFlux, Project Reactor
- **Base de Datos**: R2DBC con PostgreSQL
- **Mapeo**: MapStruct
- **Testing**: JUnit 5, Mockito, StepVerifier
- **Contenedores**: TestContainers para pruebas de integración

## 📋 Casos de Uso Implementados

### 1. Crear Orden de Ingreso Logística
```bash
POST /almacen
Content-Type: application/json

{
  "idArticulo": 123,
  "cantidad": 10.5,
  "precio": 45.99,
  "unidad": {
    "id": 1,
    "descripcion": "Kilogramos"
  },
  "fec_ingreso": "2025-01-23"
}
```

### 2. Procesar Transformación de Insumos
```bash
POST /almacen/transformacion
Content-Type: application/json

{
  "idArticulo": 456,
  "cantidad": 5.0,
  "precio": 120.00,
  "unidad": {
    "id": 2,
    "descripcion": "Metros"
  },
  "fec_ingreso": "2025-01-23",
  "details": [
    {
      "id_articulo": 789,
      "cantidad": 2.5,
      "id_unidad_consumo": 1
    }
  ]
}
```

## 🔧 Configuración

### Propiedades de Base de Datos
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/inventario
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### Configuración de Logging
```yaml
logging:
  level:
    com.walrex.module_almacen: DEBUG
    org.springframework.r2dbc: DEBUG
```

## 🧪 Testing

### Ejecutar Pruebas Unitarias
```bash
mvn clean test -pl module-almacen
```

### Ejecutar Pruebas de Integración
```bash
mvn clean verify -pl module-almacen -Pintegration-tests
```

### Cobertura de Código
```bash
mvn clean test jacoco:report -pl module-almacen
```

## 📊 Estructura de Base de Datos

### Tablas Principales
- `almacenes.ordeningreso` - Órdenes de ingreso
- `almacenes.detalle_ordeningreso` - Detalles de órdenes
- `almacenes.ordensalida` - Órdenes de salida
- `almacenes.detalle_ordensalida` - Detalles de salidas
- `almacenes.kardex` - Movimientos de inventario
- `almacenes.detalle_rollo` - Rollos de tela cruda

### Enums Importantes
```java
public enum TipoOrdenIngreso {
    LOGISTICA_GENERAL(1),
    TELA_CRUDA(2),
    TRANSFORMACION(3)
}

public enum Almacenes {
    INSUMOS(1),
    MATERIA_PRIMA(2),
    PRODUCTO_TERMINADO(3)
}
```

## 🔄 Patrón Factory Implementation

```java
// Factory para adaptadores de ingreso
@Component
public class OrdenIngresoAdapterFactoryImpl implements OrdenIngresoAdapterFactory {

    @Qualifier("logisticaGeneral")
    private final OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter;

    @Qualifier("telaCruda")
    private final OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter;

    @Qualifier("transformacion")
    private final OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter;

    @Override
    public Mono<OrdenIngresoLogisticaPort> getAdapter(TipoOrdenIngreso tipoOrden) {
        return switch (tipoOrden) {
            case TELA_CRUDA -> Mono.just(ordenIngresoTelaCrudaAdapter);
            case TRANSFORMACION -> Mono.just(ordenIngresoTransformacionAdapter);
            case LOGISTICA_GENERAL, null -> Mono.just(ordenIngresoLogisticaAdapter);
        };
    }
}
```

## 🔐 Transaccionalidad

### Operaciones Atómicas
Las transformaciones de inventario utilizan `@Transactional` para garantizar consistencia:

```java
@Service
@Transactional
public class ProcesarTransformacionService implements ProcesarTransformacionUseCase {

    @Override
    public Mono<TransformacionResponseDTO> procesarTransformacion(OrdenIngresoTransformacionDTO request) {
        return procesarIngreso(request)           // 1. Ingreso del producto resultante
                .flatMap(ingreso -> procesarSalidas(request, ingreso))  // 2. Salida de insumos
                .onErrorResume(error -> {
                    log.error("❌ Error en transformación - Rollback automático: {}", error.getMessage());
                    return Mono.error(new TransformacionException("Fallo en transformación", error));
                });
    }
}
```

## 📈 Roadmap

### ✅ Completado
- [x] CRUD básico de órdenes de ingreso
- [x] Patrón Factory para adaptadores
- [x] Gestión de tela cruda con rollos
- [x] Transformación de insumos (básico)
- [x] Pruebas unitarias principales

### 🔄 En Progreso
- [ ] Gestión completa de órdenes de salida
- [ ] Interfaz de usuario con React
- [ ] API de reportes

### 📋 Próximas Funcionalidades
- [ ] Gestión de lotes y fechas de vencimiento
- [ ] Alertas de stock mínimo
- [ ] Integración con módulo de compras
- [ ] Dashboard de inventario en tiempo real
- [ ] API REST para integración externa

## 🤝 Contribución

### Estándares de Código
- Usar arquitectura hexagonal
- Implementar pruebas unitarias (cobertura >80%)
- Seguir convenciones de naming de Spring Boot
- Documentar casos de uso complejos

### Pull Request Process
1. Fork del repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Implementar con pruebas
4. Commit con mensajes descriptivos
5. Push y crear Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para detalles.

## 📞 Contacto

Para preguntas o sugerencias sobre este módulo:
- **Email**: read424@gmail.com
- **Documentación**: [Wiki del Proyecto](link-to-wiki)
- **Issues**: [GitHub Issues](link-to-issues)

---

**Versión**: 1.0.0
**Última actualización**: Mayo 2025