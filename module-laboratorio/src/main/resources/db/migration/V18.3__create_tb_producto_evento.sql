CREATE TABLE laboratorio.tb_producto_evento (
    id_producto_evento serial4 NOT NULL,
    nombre varchar(100) NOT NULL,
    status int4 NOT NULL DEFAULT 1,
    CONSTRAINT tb_producto_evento_pkey PRIMARY KEY (id_producto_evento)
);

CREATE UNIQUE INDEX uk_tb_producto_evento_nombre_normalized
    ON laboratorio.tb_producto_evento (LOWER(TRIM(nombre)));
