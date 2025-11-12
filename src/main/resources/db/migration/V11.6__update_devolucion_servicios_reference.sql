-- ✅ Crear índice para optimizar búsquedas
CREATE INDEX idx_devolucion_servicios_motivo_devolucion ON almacenes.devolucion_servicios(id_motivo);

-- ✅ Agregar constraint de foreign key
ALTER TABLE almacenes.devolucion_servicios 
ADD CONSTRAINT fk_devolucion_servicios_motivo_devolucion 
FOREIGN KEY (id_motivo) REFERENCES almacenes.tbmotivos_devoluciones(id);

-- ✅ Comentario para documentar el cambio
COMMENT ON COLUMN almacenes.devolucion_servicios.id_motivo IS 'Referencia a motivos específicos de devolución';