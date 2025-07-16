-- V11.10__add_motivo_comprobante_devolucion_servicios.sql
-- Descripción: Agregar columna motivo_comprobante (entero) a la tabla devolucion_servicios

ALTER TABLE almacenes.devolucion_servicios 
ADD COLUMN IF NOT EXISTS motivo_comprobante INTEGER;

COMMENT ON COLUMN almacenes.devolucion_servicios.motivo_comprobante IS 'Motivo del comprobante asociado a la devolución (entero, nullable)'; 