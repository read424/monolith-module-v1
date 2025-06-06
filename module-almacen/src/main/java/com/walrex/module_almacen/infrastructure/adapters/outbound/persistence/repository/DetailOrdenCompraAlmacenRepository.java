package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailOrdenCompraAlmacenEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DetailOrdenCompraAlmacenRepository  extends ReactiveCrudRepository<DetailOrdenCompraAlmacenEntity, Long> {

    @Query("UPDATE logistica.detordencompra SET saldo=:saldoCantidad WHERE id_detordencompra=:idDetOrdenCompra")
    Mono<DetailOrdenCompraAlmacenEntity> updateSaldoIngreso(Long idDetOrdenCompra, Double saldoCantidad);
}
