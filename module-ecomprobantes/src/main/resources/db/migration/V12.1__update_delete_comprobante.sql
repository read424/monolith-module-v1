-- 1. Eliminar el trigger existente
DROP TRIGGER IF EXISTS tg_delete_comprobante ON facturacion.tbcomprobantes;

-- 2. Crear el nuevo trigger
CREATE TRIGGER tg_delete_comprobante BEFORE DELETE ON facturacion.tbcomprobantes
    FOR EACH ROW WHEN (OLD.status=1)
    EXECUTE FUNCTION facturacion.disabled_delete_guia_remision();
