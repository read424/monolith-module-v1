-- Asegura que las tablas referenciadas ('logistica.atributos') existan.
ALTER TABLE logistica.categoria_atributos
ADD CONSTRAINT fk_categoria_atributos_atributo -- Nombre para la FK a atributos
FOREIGN KEY (id_atributo) REFERENCES logistica.atributos(id_atributo);