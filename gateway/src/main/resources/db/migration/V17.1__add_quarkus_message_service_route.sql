-- Migration para agregar ruta del microservicio Quarkus de mensajería
-- El campo 'uri' contiene el nombre del servicio registrado en Consul
-- Consul resolverá dinámicamente 'quarkus-message-service' → 192.168.1.60:8088

INSERT INTO gateway.tb_modules
(module_name, uri, path, strip_prefix_count, status, ispattern, created_at, updated_at)
VALUES
(
    'module-message',                  -- Nombre lógico del módulo
    'quarkus-message-service',         -- Nombre del servicio en Consul
    'http://192.168.1.92:8000/api/message',                 -- Patrón de ruta
    NULL,                                 -- Elimina /api/message → /inbox, /send, etc.
    '1',                               -- Activo
    false,                             -- No es regex, es patrón Spring
    NOW(),
    NOW()
);

-- Comentario:
-- Cuando llegue una petición a http://localhost:8088/api/message/inbox
-- el Gateway:
-- 1. Consulta Consul para resolver 'quarkus-message-service'
-- 2. Consul responde: http://192.168.1.60:8088
-- 3. Gateway hace proxy a: http://192.168.1.60:8088/inbox
