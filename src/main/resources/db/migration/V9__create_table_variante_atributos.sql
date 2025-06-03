-- Atributos de cada variante
CREATE TABLE IF NOT EXISTS logistica.variante_atributos (
    id_variante INTEGER NOT NULL REFERENCES logistica.articulo_variantes(id_variante),
    id_atributo INTEGER NOT NULL REFERENCES logistica.atributos(id_atributo),
    id_valor_atributo INTEGER NOT NULL REFERENCES logistica.valores_atributo(id_valor_atributo),
    PRIMARY KEY (id_variante, id_atributo)
);