-- Relación entre categorías y atributos
CREATE TABLE IF NOT EXISTS logistica.categoria_atributos (
    id_categoria INTEGER NOT NULL,
    id_atributo INTEGER NOT NULL,
    es_requerido BOOLEAN DEFAULT FALSE,
    orden INTEGER,
    PRIMARY KEY (id_categoria, id_atributo)
);

-- Asegura que las tablas referenciadas ('logistica.categorias') existan.
ALTER TABLE logistica.categoria_atributos
ADD CONSTRAINT fk_categoria_atributos_categoria -- Nombre para la FK a categorias
FOREIGN KEY (id_categoria) REFERENCES logistica.categorias(id_categoria);

-- Asegura que las tablas referenciadas ('logistica.atributos') existan.
ALTER TABLE logistica.categoria_atributos
ADD CONSTRAINT fk_categoria_atributos_atributo -- Nombre para la FK a atributos
FOREIGN KEY (id_atributo) REFERENCES logistica.atributos(id_atributo);