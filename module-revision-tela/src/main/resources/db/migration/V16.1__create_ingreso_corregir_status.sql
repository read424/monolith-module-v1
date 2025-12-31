-- Tabla para almacenar correcciones de status de guías de ingreso
-- Módulo: module-revision-tela
-- Autor: Ronald E. Aybar D.
-- Fecha: 2025-12-31

CREATE TABLE IF NOT EXISTS revision_crudo.ingreso_corregir_status (
    id_ingreso_corregir_status SERIAL PRIMARY KEY,
    id_ordeningreso INTEGER NOT NULL,
    status_actual INTEGER NOT NULL,
    status_nuevo INTEGER NOT NULL,
    fec_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    procesado BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_ingreso_corregir_status_ordeningreso
        FOREIGN KEY (id_ordeningreso)
        REFERENCES almacenes.ordeningreso(id_ordeningreso)
        ON DELETE CASCADE
);

-- Índices para mejorar performance de consultas
CREATE INDEX IF NOT EXISTS idx_ingreso_corregir_status_ordeningreso
    ON revision_crudo.ingreso_corregir_status(id_ordeningreso);

CREATE INDEX IF NOT EXISTS idx_ingreso_corregir_status_procesado
    ON revision_crudo.ingreso_corregir_status(procesado);

-- Comentarios para documentación
COMMENT ON TABLE revision_crudo.ingreso_corregir_status IS
    'Tabla que almacena las correcciones de status necesarias para guías de ingreso basadas en el status de los rollos';

COMMENT ON COLUMN revision_crudo.ingreso_corregir_status.id_ordeningreso IS
    'Referencia a la orden de ingreso que requiere corrección de status';

COMMENT ON COLUMN revision_crudo.ingreso_corregir_status.status_actual IS
    'Status actual de la guía de ingreso antes de la corrección';

COMMENT ON COLUMN revision_crudo.ingreso_corregir_status.status_nuevo IS
    'Status nuevo calculado según los status de los rollos asociados';

COMMENT ON COLUMN revision_crudo.ingreso_corregir_status.procesado IS
    'Indica si la corrección ya fue aplicada a la orden de ingreso';
