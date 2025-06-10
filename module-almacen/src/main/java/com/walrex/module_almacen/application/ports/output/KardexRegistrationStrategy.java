package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import reactor.core.publisher.Mono;

public interface KardexRegistrationStrategy {
    Mono<Void> registrarKardex(DetailsIngresoEntity detalleEntity, DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso);

    // ✅ NUEVO: Método simplificado
    Mono<KardexEntity> registrarKardex(ItemKardexDTO itemKardex);
}
