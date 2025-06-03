package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.CompensacionTransaccionPort;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensacionAdapter implements CompensacionTransaccionPort {
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final DetailsIngresoRepository detailsIngresoRepository;
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detailSalidaRepository;
    private final DetailSalidaLoteRepository detailSalidaLoteRepository;
    private final ReactiveTransactionManager transactionManager;


    @Override
    public Mono<Void> compensarTransaccion(String transactionId) {
        log.info("Iniciando proceso de compensación manual para transactionId: {}", transactionId);

        // Usamos un operador transaccional para asegurar que toda la compensación sea en una sola transacción
        TransactionalOperator operator = TransactionalOperator.create(transactionManager);

        //return Mono.defer(()->{
        //    return detailSalidaRepository.deleteDetailsSalidaByIdOrden()
        //});
        return null;
    }
}
