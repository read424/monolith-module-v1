-- Elimina el prefijo 'lb://' del campo uri en gateway.tb_modules.
-- Registros que usaban el esquema lb:// de Spring Cloud LoadBalancer ahora
-- deben contener solo el nombre del servicio, que ConsulServiceRegistryAdapter
-- resuelve directamente contra la API de salud de Consul.
--
-- Ejemplo: 'lb://quarkus-almacen-service' → 'quarkus-almacen-service'

UPDATE gateway.tb_modules
SET
    uri        = SUBSTRING(uri FROM 6),  -- elimina los primeros 5 caracteres 'lb://'
    updated_at = NOW()
WHERE uri LIKE 'lb://%';
