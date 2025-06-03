package com.walrex.user.module_users.application.ports.output;

import reactor.core.publisher.Mono;

public interface RoleMessageProducer {
    public Mono<String> sendMessage(String topic, String key, String message);
}
