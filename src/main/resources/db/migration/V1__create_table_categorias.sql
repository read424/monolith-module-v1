-- Categorías
CREATE TABLE IF NOT EXISTS logistica.categorias (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria_padre_id INTEGER REFERENCES logistica.categorias(id_categoria),
    nivel INTEGER NOT NULL, -- 1: principal, 2: subcategoría, etc.
    codigo VARCHAR(20) UNIQUE,
    activo BOOLEAN DEFAULT TRUE,
    orden INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
