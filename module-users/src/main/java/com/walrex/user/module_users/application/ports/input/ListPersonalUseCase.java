package com.walrex.user.module_users.application.ports.input;

import com.walrex.user.module_users.domain.model.PagedResponse;
import com.walrex.user.module_users.domain.model.PersonalItem;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ListPersonalUseCase {
    Mono<PagedResponse<PersonalItem>> listPersonal(String search, List<Integer> idAreas, int page, int size);
}
