package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence;

import com.walrex.user.module_users.application.ports.output.ListPersonalPort;
import com.walrex.user.module_users.domain.model.PagedResponse;
import com.walrex.user.module_users.domain.model.PersonalItem;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListPersonalPersistenceAdapter implements ListPersonalPort {

    private final PersonalRepository repository;

    @Override
    public Mono<PagedResponse<PersonalItem>> listPersonal(String search, List<Integer> idAreas, int page, int size) {
        return repository.listPersonal(search, idAreas, page, size);
    }
}
