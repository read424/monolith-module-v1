package com.walrex.module_core.infrastructure.adapters.inbound.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Module Core is working!");
    }
}
