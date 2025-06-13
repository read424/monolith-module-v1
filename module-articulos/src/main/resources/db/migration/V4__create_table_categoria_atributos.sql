-- Relación entre categorías y atributos
CREATE TABLE IF NOT EXISTS logistica.categoria_atributos (
    id_categoria INTEGER NOT NULL,
    id_atributo INTEGER NOT NULL,
    es_requerido BOOLEAN DEFAULT FALSE,
    orden INTEGER,
    PRIMARY KEY (id_categoria, id_atributo)
);