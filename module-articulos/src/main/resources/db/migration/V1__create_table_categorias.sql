-- Categorías
CREATE TABLE IF NOT EXISTS logistica.categorias (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria_padre_id INTEGER,
    nivel INTEGER NOT NULL, -- 1: principal, 2: subcategoría, etc.
    codigo VARCHAR(20) UNIQUE,
    activo BOOLEAN DEFAULT TRUE,
    orden INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Se añade la restricción que apunta a sí misma.
ALTER TABLE logistica.categorias
ADD CONSTRAINT fk_categoria_padre -- Un nombre para tu restricción de clave foránea
FOREIGN KEY (categoria_padre_id) REFERENCES logistica.categorias(id_categoria);

-- Opcional: Crear un índice en la columna de la clave foránea para mejorar el rendimiento
-- Esto es una buena práctica para las columnas referenciadas por claves foráneas.
CREATE INDEX IF NOT EXISTS idx_categorias_categoria_padre_id ON logistica.categorias (categoria_padre_id);