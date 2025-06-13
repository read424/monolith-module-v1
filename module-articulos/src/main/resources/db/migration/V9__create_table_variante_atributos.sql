-- Atributos de cada variante
CREATE TABLE IF NOT EXISTS logistica.variante_atributos (
    id_variante INTEGER NOT NULL,
    id_atributo INTEGER NOT NULL,
    id_valor_atributo INTEGER NOT NULL,
    PRIMARY KEY (id_variante, id_atributo)
);

-- Restricción de clave foránea para id_variante
ALTER TABLE logistica.variante_atributos
ADD CONSTRAINT fk_variante_atributos_variante
FOREIGN KEY (id_variante) REFERENCES logistica.articulo_variantes(id_variante);

-- Restricción de clave foránea para id_atributo
ALTER TABLE logistica.variante_atributos
ADD CONSTRAINT fk_variante_atributos_atributo
FOREIGN KEY (id_atributo) REFERENCES logistica.atributos(id_atributo);

-- Restricción de clave foránea para id_valor_atributo
ALTER TABLE logistica.variante_atributos
ADD CONSTRAINT fk_variante_atributos_valor_atributo
FOREIGN KEY (id_valor_atributo) REFERENCES logistica.valores_atributo(id_valor_atributo);

-- crea un índice compuesto. Un índice adicional para id_valor_atributo es beneficioso.
CREATE INDEX IF NOT EXISTS idx_variante_atributos_id_valor_atributo ON logistica.variante_atributos (id_valor_atributo);