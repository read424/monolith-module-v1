#!/bin/bash

# Script de inicio rápido para el stack de observabilidad de Walrex
# Autor: Walrex Team
# Versión: 1.0

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Función para verificar prerrequisitos
check_prerequisites() {
    log "Verificando prerrequisitos..."
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
    fi
    
    # Verificar Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado"
    fi
    
    # Verificar que Docker esté corriendo
    if ! docker info &> /dev/null; then
        error "Docker no está corriendo"
    fi
    
    log "✅ Prerrequisitos verificados"
}

# Función para crear red si no existe
create_network() {
    log "Verificando red de Docker..."
    
    if ! docker network ls | grep -q "walrex_walrex-network"; then
        log "Creando red walrex_walrex-network..."
        docker network create walrex_walrex-network
    else
        log "✅ Red walrex_walrex-network ya existe"
    fi
}

# Función para crear directorios necesarios
create_directories() {
    log "Creando directorios necesarios..."
    
    mkdir -p loki
    mkdir -p prometheus
    mkdir -p grafana/provisioning/datasources
    mkdir -p grafana/provisioning/dashboards
    mkdir -p grafana/dashboards
    mkdir -p alertmanager
    
    log "✅ Directorios creados"
}

# Función para iniciar servicios de observabilidad
start_observability_stack() {
    log "Iniciando stack de observabilidad..."
    
    # Levantar servicios en orden
    docker-compose -f docker-compose.observability.yml up -d loki
    sleep 5
    
    docker-compose -f docker-compose.observability.yml up -d prometheus
    sleep 5
    
    docker-compose -f docker-compose.observability.yml up -d grafana
    sleep 5
    
    docker-compose -f docker-compose.observability.yml up -d promtail
    sleep 5
    
    # Levantar el resto de servicios
    docker-compose -f docker-compose.observability.yml up -d
    
    log "✅ Stack de observabilidad iniciado"
}

# Función para verificar salud de servicios
check_services_health() {
    log "Verificando salud de servicios..."
    
    # Esperar a que los servicios estén listos
    sleep 30
    
    # Verificar Loki
    if curl -s http://localhost:3100/ready | grep -q "ready"; then
        log "✅ Loki está listo"
    else
        warn "Loki no está respondiendo correctamente"
    fi
    
    # Verificar Prometheus
    if curl -s http://localhost:9090/-/healthy | grep -q "Prometheus"; then
        log "✅ Prometheus está listo"
    else
        warn "Prometheus no está respondiendo correctamente"
    fi
    
    # Verificar Grafana
    if curl -s http://localhost:3000/api/health | grep -q "ok"; then
        log "✅ Grafana está listo"
    else
        warn "Grafana no está respondiendo correctamente"
    fi
}

# Función para mostrar URLs de acceso
show_access_urls() {
    log "🎉 Stack de observabilidad configurado exitosamente!"
    echo
    echo -e "${BLUE}📊 URLs de Acceso:${NC}"
    echo -e "   • Grafana:     http://localhost:3000 (admin/admin123)"
    echo -e "   • Prometheus:  http://localhost:9090"
    echo -e "   • Loki:        http://localhost:3100"
    echo -e "   • Alertmanager: http://localhost:9093"
    echo -e "   • cAdvisor:    http://localhost:8080"
    echo -e "   • Node Exporter: http://localhost:9100"
    echo
    echo -e "${BLUE}🔧 Comandos Útiles:${NC}"
    echo -e "   • Ver logs:    docker-compose -f docker-compose.observability.yml logs -f"
    echo -e "   • Parar stack: docker-compose -f docker-compose.observability.yml down"
    echo -e "   • Reiniciar:   docker-compose -f docker-compose.observability.yml restart"
    echo
    echo -e "${BLUE}📚 Documentación:${NC}"
    echo -e "   • README:      cat OBSERVABILITY_README.md"
    echo
}

# Función para mostrar ayuda
show_help() {
    echo -e "${BLUE}🔍 Script de Inicio - Stack de Observabilidad Walrex${NC}"
    echo
    echo "Uso: $0 [OPCIÓN]"
    echo
    echo "Opciones:"
    echo "  start     Iniciar stack de observabilidad completo"
    echo "  stop      Detener stack de observabilidad"
    echo "  restart   Reiniciar stack de observabilidad"
    echo "  status    Mostrar estado de servicios"
    echo "  logs      Mostrar logs de servicios"
    echo "  clean     Limpiar volúmenes y datos"
    echo "  help      Mostrar esta ayuda"
    echo
    echo "Ejemplos:"
    echo "  $0 start    # Iniciar todo el stack"
    echo "  $0 status   # Ver estado de servicios"
    echo "  $0 logs     # Ver logs en tiempo real"
    echo
}

# Función para detener servicios
stop_services() {
    log "Deteniendo stack de observabilidad..."
    docker-compose -f docker-compose.observability.yml down
    log "✅ Stack detenido"
}

# Función para reiniciar servicios
restart_services() {
    log "Reiniciando stack de observabilidad..."
    docker-compose -f docker-compose.observability.yml restart
    log "✅ Stack reiniciado"
}

# Función para mostrar estado
show_status() {
    log "Estado de servicios:"
    docker-compose -f docker-compose.observability.yml ps
}

# Función para mostrar logs
show_logs() {
    log "Mostrando logs de servicios (Ctrl+C para salir)..."
    docker-compose -f docker-compose.observability.yml logs -f
}

# Función para limpiar datos
clean_data() {
    log "Limpiando volúmenes y datos..."
    docker-compose -f docker-compose.observability.yml down -v
    docker volume prune -f
    log "✅ Datos limpiados"
}

# Función principal
main() {
    case "${1:-start}" in
        start)
            check_prerequisites
            create_network
            create_directories
            start_observability_stack
            check_services_health
            show_access_urls
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_data
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            error "Opción no válida: $1. Usa '$0 help' para ver opciones disponibles."
            ;;
    esac
}

# Ejecutar función principal
main "$@" 