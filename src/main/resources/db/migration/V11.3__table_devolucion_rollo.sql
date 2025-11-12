CREATE TABLE IF NOT EXISTS almacenes.devolucion_rollos (
	id_devolucion_rollo serial NOT NULL,
	id_detordensalidapeso int NULL,
	id_ordeningreso int DEFAULT NULL,
	id_detordeningreso int DEFAULT NULL,
  	id_detordeningresopeso int DEFAULT NULL,
	status varchar(1) DEFAULT '1' NULL,
	create_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
	update_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT id_devolucion_rollo_pkey PRIMARY KEY (id_devolucion_rollo)
);
