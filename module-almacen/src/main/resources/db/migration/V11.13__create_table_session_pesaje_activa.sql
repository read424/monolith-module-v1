CREATE TABLE almacenes.session_pesaje_activa (
	id serial NOT NULL,
	id_detordeningreso integer NOT NULL,
	cnt_rollos integer NOT NULL,
	tot_kg double precision DEFAULT 0.00 NOT NULL,
	cnt_registro integer DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
	upated_at timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	status varchar(1) DEFAULT '1' NOT NULL,
	CONSTRAINT session_pesaje_activa_pk PRIMARY KEY (id)
);
