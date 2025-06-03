-- Valores específicos de atributos para cada artículo
CREATE TABLE IF NOT EXISTS logistica.articulo_atributos (
    id_articulo INTEGER NOT NULL REFERENCES logistica.tbarticulos(id_articulo),
    id_atributo INTEGER NOT NULL REFERENCES logistica.atributos(id_atributo),
    valor_texto VARCHAR(255),
    valor_numerico DECIMAL(15,2),
    valor_fecha DATE,
    id_valor_atributo INTEGER REFERENCES logistica.valores_atributo(id_valor_atributo),
    PRIMARY KEY (id_articulo, id_atributo)
);