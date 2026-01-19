package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import java.util.List;
import java.util.Map;

public record FilterResult(List<String> filtros,
                           Map<String, Object> parametros,
                           String whereClause) {
}
