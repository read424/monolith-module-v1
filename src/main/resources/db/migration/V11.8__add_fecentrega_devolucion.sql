-- V11.8__add_fecentrega_devolucion.sql
-- Descripción: Agregar columnas fec_entrega a la tabla devolucion_servicios

ALTER TABLE almacenes.devolucion_servicios 
ADD COLUMN IF NOT EXISTS fec_entrega DATE DEFAULT NULL;

COMMENT ON COLUMN almacenes.devolucion_servicios.fec_entrega IS 'Fecha de entrega de la devolución';