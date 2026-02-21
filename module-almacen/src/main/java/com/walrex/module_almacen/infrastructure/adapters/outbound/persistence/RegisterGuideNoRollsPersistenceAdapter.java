package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.RegisterGuideNoRollsOutputPort;
import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsDetail;
import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterGuideNoRollsPersistenceAdapter implements RegisterGuideNoRollsOutputPort {

    private final DatabaseClient databaseClient;

    @Override
    @Transactional
    public Mono<Void> saveGuide(RegisterGuideNoRollsRequest request) {
        log.info("Persistiendo guía básica: {} - {}", request.getNu_serie(), request.getNu_comprobante());

        return insertOrder(request)
                .flatMap(orderId -> insertDetails(orderId, request))
                .then();
    }

    private Mono<Integer> insertOrder(RegisterGuideNoRollsRequest request) {
        String sql = """
            INSERT INTO almacenes.ordeningreso (
                id_cliente, id_motivo, nu_serie, nu_comprobante, fec_ingreso,
                fec_ref, id_almacen, id_comprobante, status, condicion, fec_registro
            ) VALUES (
                :client, 1, :serie, :comp, :fec,
                :fec, 2, 5, 1, 1, :now
            )
            """;

        return databaseClient.sql(sql)
                .filter(statement -> statement.returnGeneratedValues("id_ordeningreso"))
                .bind("client", request.getId_cliente())
                .bind("serie", request.getNu_serie())
                .bind("comp", request.getNu_comprobante())
                .bind("fec", LocalDate.parse(request.getFec_ingreso()))
                .bind("now", OffsetDateTime.now())
                .fetch()
                .one()
                .map(row -> (Integer) row.get("id_ordeningreso"));
    }

    private Mono<Void> insertDetails(Integer orderId, RegisterGuideNoRollsRequest request) {
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            return Mono.empty();
        }

        String sql = """
            INSERT INTO almacenes.detordeningreso (
                id_ordeningreso, id_articulo, nu_rollos, peso_ref, lote,
                id_unidad, status
            ) VALUES (
                :orderId, :articulo, :rollos, :peso, :lote,
                1, 1
            )
            """;

        return Flux.fromIterable(request.getDetails())
                .flatMap(detail -> databaseClient.sql(sql)
                        .bind("orderId", orderId)
                        .bind("articulo", detail.getId_articulo())
                        .bind("rollos", detail.getNu_rollos())
                        .bind("peso", detail.getPeso_ref())
                        .bind("lote", detail.getLote())
                        .then()
                )
                .then();
    }
}
