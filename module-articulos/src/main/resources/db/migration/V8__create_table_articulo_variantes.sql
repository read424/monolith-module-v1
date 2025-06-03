-- Variantes de artículos
CREATE TABLE IF NOT EXISTS logistica.articulo_variantes (
    id_variante SERIAL PRIMARY KEY,
    id_articulo_principal INTEGER NOT NULL REFERENCES logistica.tbarticulos(id_articulo),
    sku VARCHAR(50) UNIQUE,
    precio_venta DECIMAL(12,2),
    stock_actual DECIMAL(12,2) DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);