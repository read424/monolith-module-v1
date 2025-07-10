package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.util;

import java.util.List;
import java.util.Map;

public record FilterResult(List<String> filtros,
                           Map<String, Object> parametros,
                           String whereClause) {
}
