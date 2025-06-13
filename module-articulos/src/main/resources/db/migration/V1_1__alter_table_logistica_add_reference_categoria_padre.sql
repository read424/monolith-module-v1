-- Paso 2: Añadir la restricción de clave foránea después de que la tabla haya sido creada
-- Se añade la restricción que apunta a sí misma.
ALTER TABLE logistica.categorias
ADD CONSTRAINT fk_categoria_padre -- Un nombre para tu restricción de clave foránea
FOREIGN KEY (categoria_padre_id) REFERENCES logistica.categorias(id_categoria);

-- Opcional: Crear un índice en la columna de la clave foránea para mejorar el rendimiento
-- Esto es una buena práctica para las columnas referenciadas por claves foráneas.
CREATE INDEX IF NOT EXISTS idx_categorias_categoria_padre_id ON logistica.categorias (categoria_padre_id);