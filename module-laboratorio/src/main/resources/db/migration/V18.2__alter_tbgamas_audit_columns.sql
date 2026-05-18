-- Las columnas create_at/update_at originales son TIMETZ (solo hora, sin fecha)
-- No es posible copiar a TIMESTAMPTZ, las filas existentes reciben CURRENT_TIMESTAMP por defecto

-- Agregar columnas con nombre correcto y tipo TIMESTAMPTZ
ALTER TABLE laboratorio.tbgamas ADD COLUMN created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE laboratorio.tbgamas ADD COLUMN updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

-- Eliminar columnas antiguas con tipo incorrecto
ALTER TABLE laboratorio.tbgamas DROP COLUMN create_at;
ALTER TABLE laboratorio.tbgamas DROP COLUMN update_at;
