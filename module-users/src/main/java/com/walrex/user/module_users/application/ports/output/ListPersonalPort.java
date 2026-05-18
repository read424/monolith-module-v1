package com.walrex.user.module_users.application.ports.output;

import com.walrex.user.module_users.domain.model.PagedResponse;
import com.walrex.user.module_users.domain.model.PersonalItem;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ListPersonalPort {
    Mono<PagedResponse<PersonalItem>> listPersonal(String search, List<Integer> idAreas, int page, int size);
}
