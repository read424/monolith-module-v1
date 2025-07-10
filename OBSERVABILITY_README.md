# 🔍 Stack de Observabilidad - Walrex Monolith

Este documento describe la configuración y uso del stack de observabilidad completo para el monolito modular de Walrex.

## 📋 Componentes del Stack

### 🎯 **Logging**

- **Loki**: Almacenamiento de logs agregados
- **Promtail**: Recolección y envío de logs
- **Logback**: Configuración centralizada de logging

### 📊 **Métricas**

- **Prometheus**: Recolección y almacenamiento de métricas
- **Node Exporter**: Métricas del sistema
- **cAdvisor**: Métricas de contenedores

### 📈 **Visualización**

- **Grafana**: Dashboards y visualizaciones
- **Alertmanager**: Gestión de alertas

## 🚀 Instalación y Configuración

### 1. **Prerrequisitos**

```bash
# Verificar Docker y Docker Compose
docker --version
docker-compose --version

# Crear red externa si no existe
docker network create walrex_walrex-network
```

### 2. **Levantar el Stack de Observabilidad**

```bash
# Levantar servicios de observabilidad
docker-compose -f docker-compose.observability.yml up -d

# Verificar que todos los servicios estén corriendo
docker-compose -f docker-compose.observability.yml ps
```

### 3. **Levantar la Aplicación Principal**

```bash
# Levantar el monolito
docker-compose up -d

# Verificar logs
docker-compose logs -f api-monolith
```

## 🔧 Configuración del Monolito

### **Profiles de Spring**

La aplicación está configurada con diferentes profiles:

- **`dev`**: Desarrollo local con logs detallados
- **`docker`**: Contenedores con logs JSON
- **`prod`**: Producción con logs optimizados

### **Variables de Entorno**

```bash
# En tu .env
SPRING_PROFILES_ACTIVE=docker
```

## 📊 Acceso a los Servicios

### **URLs de Acceso**

- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Loki**: http://localhost:3100
- **Alertmanager**: http://localhost:9093
- **cAdvisor**: http://localhost:8080
- **Node Exporter**: http://localhost:9100

### **Aplicación Principal**

- **API Monolith**: http://localhost:8088
- **Métricas**: http://localhost:8088/actuator/prometheus
- **Health**: http://localhost:8088/actuator/health

## 🔍 Uso de Grafana

### **1. Acceso Inicial**

```bash
# Usuario: admin
# Contraseña: admin123
```

### **2. Datasources Configurados**

- **Loki**: Para consultar logs
- **Prometheus**: Para métricas (por defecto)

### **3. Consultas de Logs Básicas**

#### **Logs por Nivel**

```logql
{service="walrex-monolith"} |= "ERROR"
{service="walrex-monolith"} |= "WARN"
{service="walrex-monolith"} |= "INFO"
```

#### **Logs por Módulo**

```logql
{service="walrex-monolith",module="almacen"}
{service="walrex-monolith",module="users"}
{service="walrex-monolith",module="gateway"}
```

#### **Logs con Correlation ID**

```logql
{service="walrex-monolith"} | json | correlationId="YOUR_CORRELATION_ID"
```

#### **Logs de Errores con Stack Trace**

```logql
{service="walrex-monolith",severity="error"} | json | line_format "{{.message}} - {{.exception}}"
```

### **4. Métricas de Prometheus**

#### **Métricas JVM**

```promql
# Uso de memoria
jvm_memory_used_bytes{application="walrex-monolith"}

# Threads activos
jvm_threads_live_threads{application="walrex-monolith"}

# Garbage Collection
jvm_gc_pause_seconds{application="walrex-monolith"}
```

#### **Métricas HTTP**

```promql
# Request rate
rate(http_server_requests_seconds_count{application="walrex-monolith"}[5m])

# Response time
histogram_quantile(0.95, http_server_requests_seconds_bucket{application="walrex-monolith"})

# Error rate
rate(http_server_requests_seconds_count{application="walrex-monolith",status=~"5.."}[5m])
```

#### **Métricas de Sistema**

```promql
# CPU usage
rate(node_cpu_seconds_total{mode!="idle"}[5m])

# Memory usage
(node_memory_MemTotal_bytes - node_memory_MemFree_bytes) / node_memory_MemTotal_bytes

# Disk usage
node_filesystem_avail_bytes{mountpoint="/"}
```

## 🔧 Troubleshooting

### **1. Problemas Comunes**

#### **Logs No Aparecen en Grafana**

```bash
# Verificar Promtail
docker logs promtail

# Verificar Loki
docker logs loki

# Verificar volúmenes
docker volume inspect walrex_app_logs
```

#### **Métricas No Aparecen**

```bash
# Verificar endpoint de métricas
curl http://localhost:8088/actuator/prometheus

# Verificar Prometheus targets
# Ir a http://localhost:9090/targets
```

#### **Servicios No Inician**

```bash
# Verificar redes
docker network ls

# Verificar logs de servicios
docker-compose -f docker-compose.observability.yml logs

# Reiniciar servicios
docker-compose -f docker-compose.observability.yml restart
```

### **2. Validar Configuración**

#### **Verificar Logs JSON**

```bash
# Ver logs en formato JSON
docker exec -it api-monolith tail -f /app/logs/application.json

# Verificar estructura de logs
docker exec -it api-monolith cat /app/logs/application.json | jq .
```

#### **Verificar Conectividad**

```bash
# Desde Promtail a Loki
docker exec -it promtail wget -O- http://loki:3100/ready

# Desde Grafana a Loki
docker exec -it grafana curl http://loki:3100/metrics
```

## 🚨 Configuración de Alertas

### **1. Alertas Básicas**

Crear archivo `prometheus/alert_rules.yml`:

```yaml
groups:
  - name: walrex_alerts
    rules:
      - alert: ApplicationDown
        expr: up{job="walrex-monolith"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Walrex Monolith is down"
          description: "The Walrex monolith has been down for more than 1 minute"

      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 10% for the last 5 minutes"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "JVM heap memory usage is above 80%"
```

### **2. Configurar Notificaciones**

Editar `alertmanager/alertmanager.yml` con tus configuraciones:

```yaml
# Para Slack
- name: "slack-notifications"
  slack_configs:
    - api_url: "YOUR_SLACK_WEBHOOK_URL"
      channel: "#alerts"

# Para Email
- name: "email-notifications"
  email_configs:
    - to: "admin@walrex.com"
      subject: "Walrex Alert: {{ .GroupLabels.alertname }}"
```

## 📚 Consultas Útiles

### **Debugging de Devolución de Rollos**

```logql
# Logs específicos del módulo almacén
{service="walrex-monolith",module="almacen"} |= "devolucion"

# Errores en servicios de devolución
{service="walrex-monolith"} |= "RegistrarDevolucionRollosService" |= "ERROR"

# Logs con correlation ID específico
{service="walrex-monolith"} | json | correlationId="YOUR_ID" | line_format "{{.timestamp}} {{.level}} {{.message}}"
```

### **Métricas de Performance**

```promql
# Response time por endpoint
histogram_quantile(0.95,
  rate(http_server_requests_seconds_bucket{
    application="walrex-monolith",
    uri="/api/almacen/rollos-disponibles"
  }[5m])
)

# Throughput por módulo
sum(rate(http_server_requests_seconds_count{application="walrex-monolith"}[5m])) by (uri)
```

## 🔄 Mantenimiento

### **Limpieza de Logs**

```bash
# Limpiar logs antiguos
docker exec -it api-monolith find /app/logs/archived -name "*.json" -mtime +30 -delete

# Limpiar datos de Loki
docker exec -it loki rm -rf /loki/chunks/*
```

### **Backup de Configuraciones**

```bash
# Backup de dashboards de Grafana
docker exec -it grafana tar -czf /tmp/dashboards-backup.tar.gz /var/lib/grafana/dashboards

# Backup de datos de Prometheus
docker exec -it prometheus tar -czf /tmp/prometheus-backup.tar.gz /prometheus
```

## 🎯 Mejores Prácticas

### **1. Logging**

- Usar correlation IDs en todas las operaciones
- Niveles de log apropiados (DEBUG, INFO, WARN, ERROR)
- Logs estructurados en JSON
- Evitar logs sensibles

### **2. Métricas**

- Métricas de negocio importantes
- Dashboards por módulo
- Alertas proactivas
- Monitoreo de SLIs/SLOs

### **3. Rendimiento**

- Usar appenders asíncronos
- Configurar límites de Loki
- Rotación de logs adecuada
- Monitoreo de recursos

## 🆘 Soporte

Para problemas específicos:

1. **Logs de errores**: Revisar `/app/logs/error.json`
2. **Métricas**: Verificar endpoint `/actuator/prometheus`
3. **Conectividad**: Verificar docker networks
4. **Configuración**: Validar archivos YAML

---

🎉 **¡Stack de Observabilidad Configurado!**

Ahora tienes visibilidad completa de tu monolito modular con logs estructurados, métricas detalladas y alertas proactivas.
