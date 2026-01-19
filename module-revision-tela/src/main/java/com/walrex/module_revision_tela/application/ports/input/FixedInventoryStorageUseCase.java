package com.walrex.module_revision_tela.application.ports.input;

import reactor.core.publisher.Mono;

public interface FixedInventoryStorageUseCase {

    Mono<Void> ejecuteRevisionLevantamiento();
}
