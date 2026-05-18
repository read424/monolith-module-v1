package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.projection;

/**
 * Projection del query JOIN multi-tabla para recetas de teñido.
 * Usar como target del mapeo en DatabaseClient (no es una @Table entity).
 */
public record RecetaProjection(
        Integer idReceta,
        String codReceta,
        String razonSocial,
        String codColores,
        String noColores,
        Integer status,
        String compartir,       // CHAR(1) en BD: 'S'/'N' o '1'/'0'
        String noGama,
        String noColor,
        String noTenido,
        String curvaDiseno      // JSONB serializado como String
) {}
