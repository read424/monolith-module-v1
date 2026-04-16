package com.walrex.module_laboratorio.infrastructure.adapters.outbound.cache.redis.adapter;

import com.walrex.module_laboratorio.application.ports.output.GamaCacheNamespacePort;
import com.walrex.module_laboratorio.application.ports.output.GamaCachePort;
import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class GamaRedisAdapter implements GamaCachePort, GamaCacheNamespacePort {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // v2: schema cambiado de List<Gama> a PagedResponse<Gama> — incrementar si cambia el schema
    private static final String CACHE_SCHEMA_VERSION = "v2";
    private static final String NS_VERSION_KEY = "gama:ns-version";
    private static final Duration TTL = Duration.ofMinutes(15);

    @Override
    public Mono<Integer> getCurrentVersion() {
        return redisTemplate.opsForValue().get(NS_VERSION_KEY)
                .map(v -> Integer.parseInt(v.toString()))
                .defaultIfEmpty(1);
    }

    @Override
    public Mono<Integer> incrementVersion() {
        return redisTemplate.opsForValue().increment(NS_VERSION_KEY)
                .map(Long::intValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> savePaged(String key, PagedResponse<Gama> paged) {
        return redisTemplate.opsForValue().set(key, paged, TTL).then();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<PagedResponse<Gama>> getPaged(String key) {
        return redisTemplate.opsForValue().get(key)
                .map(obj -> (PagedResponse<Gama>) obj);
    }

    @Override
    public Mono<Void> saveById(String key, Gama gama) {
        return redisTemplate.opsForValue().set(key, gama, TTL).then();
    }

    @Override
    public Mono<Gama> getById(String key) {
        return redisTemplate.opsForValue().get(key)
                .map(obj -> (Gama) obj);
    }
}
