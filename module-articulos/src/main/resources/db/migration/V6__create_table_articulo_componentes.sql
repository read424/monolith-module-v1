-- Para artículos compuestos/kits
CREATE TABLE IF NOT EXISTS logistica.articulo_componentes (
    id_articulo_principal INTEGER NOT NULL REFERENCES logistica.tbarticulos(id_articulo),
    id_articulo_componente INTEGER NOT NULL REFERENCES logistica.tbarticulos(id_articulo),
    cantidad DECIMAL(12,2) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_articulo_principal, id_articulo_componente)
);
