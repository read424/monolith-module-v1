CREATE TABLE IF NOT EXISTS laboratorio.curva_diseno (
    id SERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    curva_diseno JSONB NOT NULL,
    version VARCHAR(50) NOT NULL,
    id_laboratorista INTEGER NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    id_supervisor INTEGER DEFAULT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_curva_diseno_version
    ON laboratorio.curva_diseno (version);

CREATE INDEX IF NOT EXISTS idx_curva_diseno_laboratorista
    ON laboratorio.curva_diseno (id_laboratorista);
