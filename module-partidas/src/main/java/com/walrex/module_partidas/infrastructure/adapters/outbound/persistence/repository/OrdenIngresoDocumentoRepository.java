package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoDocumentoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenIngresoDocumentoRepository extends ReactiveCrudRepository<OrdenIngresoDocumentoEntity, Integer> {

}
