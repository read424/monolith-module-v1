-- V11.7__add_entregado_status_devolucion_servicios.sql
-- Descripción: Agregar columnas entregado y status a la tabla devolucion_servicios

-- Agregar columna entregado con valor por defecto 0
ALTER TABLE almacenes.devolucion_servicios 
ADD COLUMN IF NOT EXISTS entregado INTEGER DEFAULT 0;

-- Agregar columna status con valor por defecto 1
ALTER TABLE almacenes.devolucion_servicios 
ADD COLUMN IF NOT EXISTS status INTEGER DEFAULT 1;

-- Comentarios para documentación
COMMENT ON COLUMN almacenes.devolucion_servicios.entregado IS 'Indica si la devolución fue entregada/despachada (0=No, 1=Sí)';
COMMENT ON COLUMN almacenes.devolucion_servicios.status IS 'Estado de la devolución (0=Anulado, 1=Activo)';

-- Actualizar registros existentes (opcional - asegurar que tengan valores correctos)
UPDATE almacenes.devolucion_servicios 
SET entregado = 0 
WHERE entregado IS NULL;

UPDATE almacenes.devolucion_servicios 
SET status = 1 
WHERE status IS NULL;

-- Agregar constraints para asegurar valores válidos (opcional pero recomendado)
ALTER TABLE almacenes.devolucion_servicios 
ADD CONSTRAINT chk_devolucion_entregado CHECK (entregado IN (0, 1));

ALTER TABLE almacenes.devolucion_servicios 
ADD CONSTRAINT chk_devolucion_status CHECK (status IN (0, 1));