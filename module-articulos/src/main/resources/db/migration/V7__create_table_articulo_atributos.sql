-- Valores específicos de atributos para cada artículo
CREATE TABLE IF NOT EXISTS logistica.articulo_atributos (
    id_articulo INTEGER NOT NULL,
    id_atributo INTEGER NOT NULL,
    valor_texto VARCHAR(255),
    valor_numerico DECIMAL(15,2),
    valor_fecha DATE,
    id_valor_atributo INTEGER,
    PRIMARY KEY (id_articulo, id_atributo)
);

-- Restricción de clave foránea para id_articulo
ALTER TABLE logistica.articulo_atributos
ADD CONSTRAINT fk_articulo_atributos_articulo
FOREIGN KEY (id_articulo) REFERENCES logistica.tbarticulos(id_articulo);

-- Restricción de clave foránea para id_atributo
ALTER TABLE logistica.articulo_atributos
ADD CONSTRAINT fk_articulo_atributos_atributo
FOREIGN KEY (id_atributo) REFERENCES logistica.atributos(id_atributo);

-- Esta FK es opcional (puede ser NULL) porque el valor podría ser de texto/numérico/fecha
ALTER TABLE logistica.articulo_atributos
ADD CONSTRAINT fk_articulo_atributos_valor_atributo
FOREIGN KEY (id_valor_atributo) REFERENCES logistica.valores_atributo(id_valor_atributo);

-- crea un índice compuesto. Sin embargo, para id_valor_atributo, un índice es útil.
CREATE INDEX IF NOT EXISTS idx_articulo_atributos_valor_atributo ON logistica.articulo_atributos (id_valor_atributo);