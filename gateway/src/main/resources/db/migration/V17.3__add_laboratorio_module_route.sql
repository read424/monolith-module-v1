-- Migration para agregar ruta del módulo de laboratorio al Gateway
INSERT INTO gateway.tb_modules
(module_name, uri, path, strip_prefix_count, status, ispattern, route_id, created_at, updated_at)
VALUES
(
    'module-laboratorio',                  -- Nombre lógico del módulo
    NULL,
    '\/api\/v2\/laboratorio(\/.*)?',       -- Patrón de ruta (Regex)
    2,                                     -- Elimina /api/v2/laboratorio → /laboratorio
    '1',                                   -- Activo
    true,                                  -- Es regex
    'laboratorio',
    NOW(),
    NOW()
);
