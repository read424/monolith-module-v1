package com.walrex.module_laboratorio.application.service;

import com.walrex.module_laboratorio.application.ports.input.CreateCurvaDisenoUseCase;
import com.walrex.module_laboratorio.application.ports.input.GenerateCurvaDisenoPdfUseCase;
import com.walrex.module_laboratorio.application.ports.input.GetCurvaDisenoByIdUseCase;
import com.walrex.module_laboratorio.application.ports.input.ListCurvaDisenoUseCase;
import com.walrex.module_laboratorio.application.ports.output.CurvaDisenoPdfPort;
import com.walrex.module_laboratorio.application.ports.output.CurvaDisenoPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.CurvaDisenoException;
import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurvaDisenoService implements CreateCurvaDisenoUseCase, GetCurvaDisenoByIdUseCase, ListCurvaDisenoUseCase,
        GenerateCurvaDisenoPdfUseCase {

    private static final String IDEMPOTENCY_PREFIX = "idemp:laboratorio:curva-diseno:";
    private static final String PROCESSING = "PROCESSING";

    private final CurvaDisenoPersistencePort persistencePort;
    private final CurvaDisenoPdfPort pdfPort;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<CurvaDiseno> create(CurvaDiseno curvaDiseno, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.error(new CurvaDisenoException("El header Idempotency-Key es obligatorio", "IDEMPOTENCY_KEY_REQUIRED"));
        }

        String key = IDEMPOTENCY_PREFIX + idempotencyKey.trim();
        return redisTemplate.opsForValue()
                .setIfAbsent(key, PROCESSING, Duration.ofMinutes(5))
                .flatMap(created -> Boolean.TRUE.equals(created)
                        ? createAndStoreIdempotencyResult(curvaDiseno, key)
                        : resolveIdempotentRetry(key));
    }

    @Override
    public Mono<CurvaDiseno> getById(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new CurvaDisenoException("Curva de diseño no encontrada", "NOT_FOUND")));
    }

    @Override
    public Mono<PagedResponse<CurvaDiseno>> listAll(String search, int page, int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);
        return Mono.zip(
                        persistencePort.findAll(search, normalizedPage, normalizedSize).collectList(),
                        persistencePort.countAll(search)
                )
                .map(tuple -> PagedResponse.of(tuple.getT1(), normalizedPage, normalizedSize, tuple.getT2()));
    }

    @Override
    public Mono<byte[]> generatePdf(Integer id) {
        return getById(id)
                .flatMap(curvaDiseno -> {
                    if (curvaDiseno.getCurvaDiseno() == null || curvaDiseno.getCurvaDiseno().isBlank()) {
                        return Mono.error(new CurvaDisenoException(
                                "La curva_diseno no tiene JSON registrado", "CURVA_DISENO_EMPTY"));
                    }
                    return pdfPort.generatePdf(curvaDiseno);
                });
    }

    private Mono<CurvaDiseno> createAndStoreIdempotencyResult(CurvaDiseno curvaDiseno, String key) {
        curvaDiseno.setStatus(0);
        curvaDiseno.setLocked(false);
        curvaDiseno.setCreatedAt(OffsetDateTime.now());
        curvaDiseno.setUpdatedAt(OffsetDateTime.now());

        return persistencePort.save(curvaDiseno)
                .flatMap(saved -> redisTemplate.opsForValue()
                        .set(key, saved.getId().toString(), Duration.ofHours(24))
                        .thenReturn(saved))
                .onErrorResume(error -> redisTemplate.delete(key).then(Mono.error(error)));
    }

    private Mono<CurvaDiseno> resolveIdempotentRetry(String key) {
        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(value -> {
                    if (PROCESSING.equals(value)) {
                        return Mono.error(new CurvaDisenoException(
                                "La solicitud con el mismo Idempotency-Key sigue en proceso", "IDEMPOTENCY_CONFLICT"));
                    }
                    try {
                        return getById(Integer.valueOf(value));
                    } catch (NumberFormatException ex) {
                        log.warn("Valor de idempotencia inválido para key {}: {}", key, value);
                        return Mono.error(new CurvaDisenoException("Idempotency-Key inválido", "IDEMPOTENCY_CONFLICT"));
                    }
                });
    }
}
