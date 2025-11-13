-- Migration: V16.1
-- Description: Crear tabla solicitudes_cambio_servicios_partidas en schema produccion
-- Author: Sistema
-- Date: 2025-10-28
--
-- Esta tabla almacena las solicitudes de cambio de servicios para partidas,
-- incluyendo los valores antiguos (old) y los nuevos valores propuestos.

CREATE TABLE IF NOT EXISTS produccion.solicitudes_cambio_servicios_partidas (
    -- Identificador único de la solicitud
    id SERIAL PRIMARY KEY,

    -- Datos de la partida afectada
    id_partida INTEGER NOT NULL,

    -- Valores antiguos (OLD) - Estado actual antes del cambio
    id_ordenproduccion_old INTEGER NOT NULL,
    id_orden_old INTEGER NOT NULL,
    id_det_os_old INTEGER NOT NULL,
    id_precio_old INTEGER NOT NULL,
    precio_old DOUBLE PRECISION NOT NULL,
    id_gama_old INTEGER NOT NULL,
    id_ruta_old INTEGER NOT NULL,
    desc_articulo_old TEXT NOT NULL,

    -- Valores nuevos propuestos (pueden ser NULL si aún no se aprueban)
    id_ordenproduccion INTEGER DEFAULT NULL,
    id_orden INTEGER DEFAULT NULL,
    id_det_os INTEGER DEFAULT NULL,
    id_ruta INTEGER DEFAULT NULL,
    id_gama INTEGER DEFAULT NULL,
    id_precio INTEGER DEFAULT NULL,
    precio DOUBLE PRECISION DEFAULT NULL,

    -- Estado y control de aprobación
    status INTEGER DEFAULT 1,                    -- 1: Activo, 0: Inactivo
    aprobado INTEGER DEFAULT 0,                  -- 0: No aprobado, 1: Aprobado
    por_aprobar INTEGER DEFAULT 1,               -- 1: Pendiente de aprobación, 0: No requiere aprobación
    partidas_adicionales INTEGER DEFAULT 0,      -- Contador de partidas adicionales afectadas

    -- Datos de usuario
    id_usuario INTEGER NOT NULL,                 -- Usuario que creó la solicitud
    id_usuario_autorizado INTEGER DEFAULT NULL,  -- Usuario que autorizó la solicitud

    -- Auditoría
    fec_registro DATE,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP
);

-- Índices para mejorar el rendimiento de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_solicitudes_cambio_id_partida
    ON produccion.solicitudes_cambio_servicios_partidas(id_partida);

CREATE INDEX IF NOT EXISTS idx_solicitudes_cambio_status
    ON produccion.solicitudes_cambio_servicios_partidas(status);

CREATE INDEX IF NOT EXISTS idx_solicitudes_cambio_aprobado
    ON produccion.solicitudes_cambio_servicios_partidas(aprobado);

CREATE INDEX IF NOT EXISTS idx_solicitudes_cambio_por_aprobar
    ON produccion.solicitudes_cambio_servicios_partidas(por_aprobar);

CREATE INDEX IF NOT EXISTS idx_solicitudes_cambio_id_usuario
    ON produccion.solicitudes_cambio_servicios_partidas(id_usuario);

-- Comentarios para documentación de la tabla
COMMENT ON TABLE produccion.solicitudes_cambio_servicios_partidas IS
    'Almacena las solicitudes de cambio de servicios para partidas de producción';

COMMENT ON COLUMN produccion.solicitudes_cambio_servicios_partidas.id IS
    'Identificador único de la solicitud (PK)';

COMMENT ON COLUMN produccion.solicitudes_cambio_servicios_partidas.id_partida IS
    'ID de la partida afectada por el cambio';

COMMENT ON COLUMN produccion.solicitudes_cambio_servicios_partidas.aprobado IS
    'Estado de aprobación: 0=No aprobado, 1=Aprobado';

COMMENT ON COLUMN produccion.solicitudes_cambio_servicios_partidas.por_aprobar IS
    'Indicador de si requiere aprobación: 1=Pendiente, 0=No requiere';