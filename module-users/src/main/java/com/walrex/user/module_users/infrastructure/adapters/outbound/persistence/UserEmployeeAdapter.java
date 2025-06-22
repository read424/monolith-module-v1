package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence;

import com.walrex.user.module_users.application.ports.output.UserDetailOutputPort;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.projection.UserEmployee;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository.UserEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserEmployeeAdapter implements UserDetailOutputPort {
    private final UserEmployeeRepository userEmployeeRepository;
    @Override
    public Mono<UserEmployee> findByUsername(String username){
        return userEmployeeRepository.findEmployeeByUserName(username);
    }
}
