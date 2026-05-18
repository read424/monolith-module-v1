ALTER TABLE produccion.tb_partidas
ADD COLUMN IF NOT EXISTS curva_diseno JSONB NULL;
