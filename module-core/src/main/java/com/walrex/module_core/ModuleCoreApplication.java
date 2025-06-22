package com.walrex.module_core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { RedisRepositoriesAutoConfiguration.class})
@EnableR2dbcRepositories(basePackages = {
		"com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository",
		"com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository",
		"com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository",
		"com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository",
		"com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository"
})
@ComponentScan(basePackages = {
		"com.walrex",
		"com.walrex.module_core",
		"com.walrex.gateway",
		"com.walrex.module_common",
		"com.walrex.user",
		"com.walrex.role"
})
@Slf4j
public class ModuleCoreApplication {

	public static void main(String[] args) {
		log.info("Iniciando aplicación...");
		try {
			System.setProperty("reactor.tools.agent.enabled", "false");
			SpringApplication.run(ModuleCoreApplication.class, args);
			log.info("Aplicación iniciada correctamente");
		} catch (Exception e) {
			log.error("Error al iniciar la aplicación", e);
			e.printStackTrace();
		}
	}
}
