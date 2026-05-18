package com.walrex.module_laboratorio.infrastructure.adapters.outbound.cache;

import com.walrex.module_laboratorio.application.ports.output.EtapaTinturaCachePort;
import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EtapaTinturaRedisAdapter implements EtapaTinturaCachePort {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Mono<Void> saveAll(String key, List<EtapaTintura> etapas) {
        return redisTemplate.opsForValue().set(key, etapas, CACHE_TTL).then();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<EtapaTintura> findAll(String key) {
        return redisTemplate.opsForValue().get(key)
                .flatMapMany(obj -> {
                    if (obj instanceof List) {
                        return Flux.fromIterable((List<EtapaTintura>) obj);
                    }
                    return Flux.empty();
                });
    }

    @Override
    public Mono<Void> invalidate(String keyPattern) {
        return redisTemplate.keys(keyPattern)
                .flatMap(redisTemplate::delete)
                .then();
    }
}
