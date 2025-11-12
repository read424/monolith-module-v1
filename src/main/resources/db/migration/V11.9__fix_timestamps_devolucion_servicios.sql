-- V11.9__fix_timestamps_devolucion_servicios.sql
-- Descripción: Corregir campos de timestamp en devolucion_servicios para que funcionen automáticamente

-- 1. Asegurar que los campos tengan valores por defecto correctos
ALTER TABLE almacenes.devolucion_servicios 
ALTER COLUMN create_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE almacenes.devolucion_servicios 
ALTER COLUMN update_at SET DEFAULT CURRENT_TIMESTAMP;

-- 2. Actualizar registros existentes que tengan NULL en timestamps
UPDATE almacenes.devolucion_servicios 
SET create_at = CURRENT_TIMESTAMP 
WHERE create_at IS NULL;

UPDATE almacenes.devolucion_servicios 
SET update_at = CURRENT_TIMESTAMP 
WHERE update_at IS NULL;

-- 3. Usar la función existente para actualizar automáticamente update_at
-- La función public.date_update_at() ya existe y es correcta

-- 4. Crear trigger para actualizar update_at automáticamente
DROP TRIGGER IF EXISTS update_devolucion_servicios_updated_at ON almacenes.devolucion_servicios;

CREATE TRIGGER update_devolucion_servicios_updated_at
    BEFORE UPDATE ON almacenes.devolucion_servicios
    FOR EACH ROW
    EXECUTE FUNCTION public.date_update_at();

-- 5. Comentarios para documentación
COMMENT ON COLUMN almacenes.devolucion_servicios.create_at IS 'Fecha y hora de creación del registro (se establece automáticamente)';
COMMENT ON COLUMN almacenes.devolucion_servicios.update_at IS 'Fecha y hora de última actualización (se actualiza automáticamente)'; 