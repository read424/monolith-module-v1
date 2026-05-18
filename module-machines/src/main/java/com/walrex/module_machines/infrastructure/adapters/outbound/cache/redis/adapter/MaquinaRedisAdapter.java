package com.walrex.module_machines.infrastructure.adapters.outbound.cache.redis.adapter;

import com.walrex.module_machines.application.ports.output.MaquinaCachePort;
import com.walrex.module_machines.domain.model.Maquina;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MaquinaRedisAdapter implements MaquinaCachePort {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "machines:ubicacion:";

    private String key(Integer idUbicacion) {
        return KEY_PREFIX + idUbicacion;
    }

    @Override
    public Mono<Void> saveAll(Integer idUbicacion, List<Maquina> maquinas) {
        return redisTemplate.opsForValue().set(key(idUbicacion), maquinas, TTL).then();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<List<Maquina>> getAll(Integer idUbicacion) {
        return redisTemplate.opsForValue().get(key(idUbicacion))
                .map(obj -> (List<Maquina>) obj);
    }
}
