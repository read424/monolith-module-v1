CREATE TABLE IF NOT EXISTS produccion.declaracion_calidad (
    id                  SERIAL PRIMARY KEY,
    id_ubicacion        INTEGER NOT NULL,
    fecha_declaracion   DATE NOT NULL,
    id_partida          INTEGER NOT NULL,
    id_maquina          INTEGER,
    auditor             VARCHAR(255),
    nivel_critico       INTEGER,
    id_motivo_rechazo   INTEGER,
    is_observado        INTEGER NOT NULL DEFAULT 0,
    observacion         TEXT,
    cnt_rollos          INTEGER,
    status              INTEGER NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMPTZ DEFAULT NULL,
    CONSTRAINT uq_declaracion_calidad UNIQUE (id_ubicacion, fecha_declaracion, id_partida)
);

CREATE INDEX IF NOT EXISTS idx_dc_id_partida   ON produccion.declaracion_calidad (id_partida);
CREATE INDEX IF NOT EXISTS idx_dc_id_ubicacion ON produccion.declaracion_calidad (id_ubicacion);
CREATE INDEX IF NOT EXISTS idx_dc_fecha        ON produccion.declaracion_calidad (fecha_declaracion);
