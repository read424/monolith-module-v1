ALTER TABLE produccion.declaracion_calidad
    DROP COLUMN IF EXISTS auditor,
    ADD COLUMN IF NOT EXISTS id_auditor INTEGER;
