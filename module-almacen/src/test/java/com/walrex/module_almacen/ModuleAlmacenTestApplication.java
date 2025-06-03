package com.walrex.module_almacen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.walrex.module_almacen.infrastructure.config",
                "com.walrex.module_almacen.infrastructure.adapters.outbound.persistence",
                "com.walrex.module_almacen.domain"
        }
)
public class ModuleAlmacenTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModuleAlmacenTestApplication.class, args);
    }
}
