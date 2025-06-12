package com.walrex.module_core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.walrex",
		"com.walrex.module_core",
		"com.walrex.gateway.gateway",
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
