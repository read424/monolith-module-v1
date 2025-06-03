CREATE TABLE IF NOT EXISTS logistica.atributos (
    id_atributo SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(20) NOT NULL, -- texto, numero, boolean, fecha, lista, etc.
    mostrar_en_filtros BOOLEAN DEFAULT FALSE,
    mostrar_en_ficha BOOLEAN DEFAULT TRUE,
    es_requerido BOOLEAN DEFAULT FALSE,
    orden INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
