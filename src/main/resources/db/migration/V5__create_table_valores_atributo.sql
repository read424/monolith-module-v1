-- Valores predefinidos para atributos tipo lista
CREATE TABLE IF NOT EXISTS logistica.valores_atributo (
    id_valor_atributo SERIAL PRIMARY KEY,
    id_atributo INTEGER NOT NULL REFERENCES logistica.atributos(id_atributo),
    valor VARCHAR(100) NOT NULL,
    codigo VARCHAR(20),
    orden INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);