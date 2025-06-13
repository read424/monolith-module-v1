-- Variantes de artículos
CREATE TABLE IF NOT EXISTS logistica.articulo_variantes (
    id_variante SERIAL PRIMARY KEY,
    id_articulo_principal INTEGER NOT NULL,
    sku VARCHAR(50) UNIQUE,
    precio_venta DECIMAL(12,2),
    stock_actual DECIMAL(12,2) DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Asegura que la tabla referenciada ('logistica.tbarticulos') exista.
ALTER TABLE logistica.articulo_variantes
ADD CONSTRAINT fk_articulo_variantes_principal -- Nombre para tu restricción de clave foránea
FOREIGN KEY (id_articulo_principal) REFERENCES logistica.tbarticulos(id_articulo);

-- Opcional: Crear un índice en la columna de la clave foránea para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_articulo_variantes_id_articulo_principal ON logistica.articulo_variantes (id_articulo_principal);