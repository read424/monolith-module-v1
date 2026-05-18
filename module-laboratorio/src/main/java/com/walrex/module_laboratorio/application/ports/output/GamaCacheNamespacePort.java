package com.walrex.module_laboratorio.application.ports.output;
import reactor.core.publisher.Mono;
public interface GamaCacheNamespacePort {
    Mono<Integer> getCurrentVersion();
    Mono<Integer> incrementVersion();
}
