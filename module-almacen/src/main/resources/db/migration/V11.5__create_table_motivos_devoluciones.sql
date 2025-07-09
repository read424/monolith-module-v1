-- ✅ Tabla específica para motivos de devolución
CREATE TABLE almacenes.tbmotivos_devoluciones (
    id serial NOT NULL,
    descripcion varchar(255) NOT NULL,
    status int NOT NULL DEFAULT 1,
    create_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tbmotivos_devoluciones PRIMARY KEY (id)
);

-- ✅ Índices para optimizar consultas
CREATE INDEX idx_tbmotivos_devoluciones_status ON almacenes.tbmotivos_devoluciones(status);
CREATE INDEX idx_tbmotivos_devoluciones_descripcion ON almacenes.tbmotivos_devoluciones(descripcion);

-- ✅ Comentarios para documentación
COMMENT ON TABLE almacenes.tbmotivos_devoluciones IS 'Motivos específicos para devoluciones de mercancía';
COMMENT ON COLUMN almacenes.tbmotivos_devoluciones.descripcion IS 'Descripción del motivo de devolución';
COMMENT ON COLUMN almacenes.tbmotivos_devoluciones.status IS '1=Activo, 0=Inactivo';
