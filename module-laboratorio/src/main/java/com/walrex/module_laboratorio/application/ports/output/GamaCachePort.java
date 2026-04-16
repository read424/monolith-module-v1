package com.walrex.module_laboratorio.application.ports.output;
import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import reactor.core.publisher.Mono;
public interface GamaCachePort {
    Mono<Void> savePaged(String key, PagedResponse<Gama> paged);
    Mono<PagedResponse<Gama>> getPaged(String key);
    Mono<Void> saveById(String key, Gama gama);
    Mono<Gama> getById(String key);
}
