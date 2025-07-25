-- Agregar REPLICA IDENTITY a la tabla tb_conductor para permitir operaciones de delete
ALTER TABLE ventas.tb_conductor REPLICA IDENTITY DEFAULT; 