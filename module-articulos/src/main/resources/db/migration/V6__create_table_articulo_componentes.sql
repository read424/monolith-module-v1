-- Para artículos compuestos/kits
CREATE TABLE IF NOT EXISTS logistica.articulo_componentes (
    id_articulo_principal INTEGER NOT NULL,
    id_articulo_componente INTEGER NOT NULL,
    cantidad DECIMAL(12,2) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_articulo_principal, id_articulo_componente)
);

-- Asegura que la tabla referenciada ('logistica.tbarticulos') exista.
ALTER TABLE logistica.articulo_componentes
ADD CONSTRAINT fk_articulo_principal -- Nombre para la FK a tbarticulos (articulo_principal)
FOREIGN KEY (id_articulo_principal) REFERENCES logistica.tbarticulos(id_articulo);

ALTER TABLE logistica.articulo_componentes
ADD CONSTRAINT fk_articulo_componente -- Nombre para la FK a tbarticulos (articulo_componente)
FOREIGN KEY (id_articulo_componente) REFERENCES logistica.tbarticulos(id_articulo);

-- En este caso, id_articulo_principal e id_articulo_componente ya forman la clave primaria,
-- lo que implícitamente crea un índice compuesto. Sin embargo, para mejorar el rendimiento
-- de búsquedas específicas en una sola columna FK, se pueden añadir índices individuales.
CREATE INDEX IF NOT EXISTS idx_articulo_componentes_principal ON logistica.articulo_componentes (id_articulo_principal);
CREATE INDEX IF NOT EXISTS idx_articulo_componentes_componente ON logistica.articulo_componentes (id_articulo_componente);