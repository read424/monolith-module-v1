package com.walrex.module_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { RedisRepositoriesAutoConfiguration.class })
@EnableR2dbcRepositories(basePackages = "com.walrex")
@ComponentScan(basePackages = "com.walrex")
@Slf4j
public class ModuleCoreApplication {

    public static void main(String[] args) {
        log.info("Iniciando aplicación...");
        // Solo cargar .env en desarrollo
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(activeProfile)) {
            loadDotEnv();
        }

        try {
            System.setProperty("reactor.tools.agent.enabled", "false");
            SpringApplication.run(ModuleCoreApplication.class, args);
            log.info("Aplicación iniciada correctamente");
        } catch (Exception e) {
            log.error("Error al iniciar la aplicación", e);
            e.printStackTrace();
        }
    }

    private static void loadDotEnv() {
        try {
            // Solo se ejecuta si la clase está disponible
            Class.forName("io.github.cdimascio.dotenv.Dotenv");

            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            log.info("Variables de entorno cargadas desde .env");
        } catch (ClassNotFoundException e) {
            log.info("Dotenv no disponible, usando variables del sistema");
        } catch (Exception e) {
            log.warn("No se pudo cargar archivo .env: {}", e.getMessage());
        }
    }
}
