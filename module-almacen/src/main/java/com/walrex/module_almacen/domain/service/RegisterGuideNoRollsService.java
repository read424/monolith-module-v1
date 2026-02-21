package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.RegisterGuideNoRollsUseCase;
import com.walrex.module_almacen.application.ports.output.RegisterGuideNoRollsOutputPort;
import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterGuideNoRollsService implements RegisterGuideNoRollsUseCase {

    private final RegisterGuideNoRollsOutputPort outputPort;
    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String IDEMPOTENCY_PREFIX = "idemp:guide:";

    @Override
    public Mono<Void> registerGuide(RegisterGuideNoRollsRequest request) {
        String key = IDEMPOTENCY_PREFIX + request.getRequest_id();

        return redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5))
                .flatMap(isAbsent -> {
                    if (Boolean.TRUE.equals(isAbsent)) {
                        log.info("Token de idempotencia válido: {}. Procesando registro.", request.getRequest_id());
                        return processRegistration(request);
                    } else {
                        log.warn("Conflicto de idempotencia detectado para request_id: {}", request.getRequest_id());
                        return Mono.error(new RuntimeException("Idempotency conflict: Request already processed or in progress."));
                    }
                });
    }

    private Mono<Void> processRegistration(RegisterGuideNoRollsRequest request) {
        if (request.getDetails() != null) {
            String baseLote = request.getNu_comprobante();
            for (int i = 0; i < request.getDetails().size(); i++) {
                request.getDetails().get(i).setLote(baseLote + "-" + (i + 1));
            }
        }

        return outputPort.saveGuide(request);
    }
}
