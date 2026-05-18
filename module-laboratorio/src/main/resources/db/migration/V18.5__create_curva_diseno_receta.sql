CREATE TABLE IF NOT EXISTS laboratorio.curva_diseno_receta (
    id              SERIAL PRIMARY KEY,
    id_receta       INTEGER NOT NULL,
    id_curva_diseno INTEGER NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cdr_receta        FOREIGN KEY (id_receta)       REFERENCES laboratorio.tb_receta(id_receta),
    CONSTRAINT fk_cdr_curva_diseno  FOREIGN KEY (id_curva_diseno) REFERENCES laboratorio.curva_diseno(id)
);

CREATE INDEX IF NOT EXISTS idx_cdr_receta       ON laboratorio.curva_diseno_receta (id_receta);
CREATE INDEX IF NOT EXISTS idx_cdr_curva_diseno ON laboratorio.curva_diseno_receta (id_curva_diseno);
