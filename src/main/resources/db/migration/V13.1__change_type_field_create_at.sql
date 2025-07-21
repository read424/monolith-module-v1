ALTER TABLE ventas.tb_conductor
ALTER COLUMN create_at TYPE TIMESTAMP USING create_at::timestamp; 