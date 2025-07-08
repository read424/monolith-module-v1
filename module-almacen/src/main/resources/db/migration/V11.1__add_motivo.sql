INSERT INTO almacenes.tbmotivos (id_motivo, no_motivo, status) VALUES (33, 'DEVOLUCION DE SERVICIO', 1) ON CONFLICT ON CONSTRAINT tbmotivos_pkey DO NOTHING;
