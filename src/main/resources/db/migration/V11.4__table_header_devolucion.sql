CREATE TABLE IF NOT EXISTS almacenes.devolucion_servicios(
  id_devolucion serial NOT NULL,
  id_ordensalida int NOT NULL,
  id_motivo int NOT NULL,
  id_comprobante int NULL,
  id_empresa_transp int NULL,
  id_modalidad int NULL,
  id_tip_doc_chofer int NULL,
  num_doc_chofer varchar(12) NULL,
  num_placa varchar(12) NULL,
  id_llegada int NULL,
  observacion text,
  id_usuario int NOT NULL,
	create_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
	update_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT id_devolucion_pkey PRIMARY KEY (id_devolucion)
);
