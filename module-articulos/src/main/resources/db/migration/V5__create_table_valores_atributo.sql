-- Valores predefinidos para atributos tipo lista
CREATE TABLE IF NOT EXISTS logistica.valores_atributo (
    id_valor_atributo SERIAL PRIMARY KEY,
    id_atributo INTEGER NOT NULL,
    valor VARCHAR(100) NOT NULL,
    codigo VARCHAR(20),
    orden INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Asegura que la tabla referenciada ('logistica.atributos') exista.
ALTER TABLE logistica.valores_atributo
ADD CONSTRAINT fk_valores_atributo_atributo -- Un nombre para tu restricción de clave foránea
FOREIGN KEY (id_atributo) REFERENCES logistica.atributos(id_atributo);

-- Crear un índice en la columna de la clave foránea para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_valores_atributo_id_atributo ON logistica.valores_atributo (id_atributo);