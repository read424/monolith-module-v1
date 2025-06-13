-- Asegura que las tablas referenciadas ('logistica.categorias') existan.
ALTER TABLE logistica.categoria_atributos
ADD CONSTRAINT fk_categoria_atributos_categoria -- Nombre para la FK a categorias
FOREIGN KEY (id_categoria) REFERENCES logistica.categorias(id_categoria);
