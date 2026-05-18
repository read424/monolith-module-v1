INSERT INTO gateway.tb_modules
(module_name, uri, path, strip_prefix_count, status, ispattern, route_id, created_at, updated_at)
VALUES
(
    'module-machines',
    NULL,
    '\/api\/v2\/machines(\/.*)?',
    2,
    '1',
    true,
    'machines',
    NOW(),
    NOW()
)
ON CONFLICT ON CONSTRAINT tb_modules_pk DO NOTHING;
