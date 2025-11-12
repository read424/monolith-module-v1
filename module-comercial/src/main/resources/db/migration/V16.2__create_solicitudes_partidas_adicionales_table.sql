-- Migration: V16.2
-- Description: Crear tabla solicitudes_partidas_adicionales en schema produccion
-- Author: Sistema
-- Date: 2025-10-28
--
-- Esta tabla almacena las partidas adicionales afectadas por una solicitud de cambio de servicio.
-- Relación N:1 con solicitudes_cambio_servicios_partidas.

CREATE TABLE IF NOT EXISTS produccion.solicitudes_partidas_adicionales (
    -- Identificador único de la partida adicional
    id SERIAL PRIMARY KEY,

    -- Relación con la solicitud principal (Foreign Key)
    id_solicitud INTEGER NOT NULL,

    -- Datos de la partida adicional
    id_partida INTEGER NOT NULL,

    -- Estado y control de aprobación
    status INTEGER DEFAULT 1,       -- 1: Activo, 0: Inactivo
    aprobado INTEGER DEFAULT 0,     -- 0: No aprobado, 1: Aprobado

    -- Auditoría
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP,

    -- Foreign Key constraint hacia la tabla principal
    CONSTRAINT fk_solicitudes_partidas_adicionales_solicitud
        FOREIGN KEY (id_solicitud)
        REFERENCES produccion.solicitudes_cambio_servicios_partidas(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Índices para mejorar el rendimiento de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_solicitudes_partidas_adicionales_id_solicitud
    ON produccion.solicitudes_partidas_adicionales(id_solicitud);

CREATE INDEX IF NOT EXISTS idx_solicitudes_partidas_adicionales_id_partida
    ON produccion.solicitudes_partidas_adicionales(id_partida);

CREATE INDEX IF NOT EXISTS idx_solicitudes_partidas_adicionales_status
    ON produccion.solicitudes_partidas_adicionales(status);

CREATE INDEX IF NOT EXISTS idx_solicitudes_partidas_adicionales_aprobado
    ON produccion.solicitudes_partidas_adicionales(aprobado);

-- Índice compuesto para consultas frecuentes de solicitud + partida
CREATE INDEX IF NOT EXISTS idx_solicitudes_partidas_adicionales_solicitud_partida
    ON produccion.solicitudes_partidas_adicionales(id_solicitud, id_partida);

-- Comentarios para documentación de la tabla
COMMENT ON TABLE produccion.solicitudes_partidas_adicionales IS
    'Almacena las partidas adicionales afectadas por una solicitud de cambio de servicio';

COMMENT ON COLUMN produccion.solicitudes_partidas_adicionales.id IS
    'Identificador único de la partida adicional (PK)';

COMMENT ON COLUMN produccion.solicitudes_partidas_adicionales.id_solicitud IS
    'ID de la solicitud de cambio principal (FK a solicitudes_cambio_servicios_partidas)';

COMMENT ON COLUMN produccion.solicitudes_partidas_adicionales.id_partida IS
    'ID de la partida adicional afectada por el cambio';

COMMENT ON COLUMN produccion.solicitudes_partidas_adicionales.aprobado IS
    'Estado de aprobación: 0=No aprobado, 1=Aprobado';

COMMENT ON CONSTRAINT fk_solicitudes_partidas_adicionales_solicitud
    ON produccion.solicitudes_partidas_adicionales IS
    'Foreign key hacia la solicitud de cambio principal. ON DELETE CASCADE elimina las partidas adicionales si se elimina la solicitud principal.';