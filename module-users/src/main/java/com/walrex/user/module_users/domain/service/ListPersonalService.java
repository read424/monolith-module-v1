package com.walrex.user.module_users.domain.service;

import com.walrex.user.module_users.application.ports.input.ListPersonalUseCase;
import com.walrex.user.module_users.application.ports.output.ListPersonalPort;
import com.walrex.user.module_users.domain.model.PagedResponse;
import com.walrex.user.module_users.domain.model.PersonalItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPersonalService implements ListPersonalUseCase {

    private final ListPersonalPort listPersonalPort;

    @Override
    public Mono<PagedResponse<PersonalItem>> listPersonal(String search, List<Integer> idAreas, int page, int size) {
        return listPersonalPort.listPersonal(search, idAreas, page, size);
    }
}
