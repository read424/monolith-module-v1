package com.walrex.module_core.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.LogstashEncoder;

@Configuration
@Slf4j
public class DynamicLoggingConfig {

    @Autowired
    private Environment environment;

    @Value("${logging.dynamic.enabled:true}")
    private boolean dynamicLoggingEnabled;

    @Value("${logging.dynamic.default-level:DEBUG}")
    private String defaultLevel;

    @Value("${logging.dynamic.error-level:ERROR}")
    private String errorLevel;

    @Value("${logging.dynamic.modules:almacen,users,articulos,ecomprobantes,role,gateway,core}")
    private String[] configuredModules;

    @PostConstruct
    public void configureDynamicLogging() {
        if (!dynamicLoggingEnabled) {
            log.info("🚫 Logging dinámico deshabilitado");
            return;
        }

        log.info("🔧 Configurando logging dinámico para {} módulos", configuredModules.length);

        // Mapear nombres de módulos a packages
        for (String moduleName : configuredModules) {
            String modulePackage = getModulePackage(moduleName);
            configureModuleLogger(modulePackage);
        }
    }

    private String getModulePackage(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "almacen":
                return "com.walrex.module_almacen";
            case "users":
                return "com.walrex.module_users";
            case "articulos":
                return "com.walrex.module_articulos";
            case "ecomprobantes":
                return "com.walrex.module_ecomprobantes";
            case "role":
                return "com.walrex.role.module_role";
            case "gateway":
                return "com.walrex.gateway";
            case "core":
                return "com.walrex.module_core";
            default:
                log.warn("⚠️ Módulo no reconocido: {}", moduleName);
                return null;
        }
    }

    private void configureModuleLogger(String modulePackage) {
        if (modulePackage == null) {
            return;
        }

        // Extraer nombre del módulo del package
        String moduleName = extractModuleName(modulePackage);

        // Configurar logger específico para el módulo
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger moduleLogger = loggerContext.getLogger(modulePackage);

        // Configurar appender específico para el módulo
        configureModuleAppender(loggerContext, moduleName, moduleLogger);
    }

    private String extractModuleName(String packageName) {
        if (packageName.contains("module_")) {
            return packageName.substring(packageName.lastIndexOf("module_") + 7);
        } else if (packageName.contains("gateway")) {
            return "gateway";
        } else {
            return "core";
        }
    }

    private void configureModuleAppender(LoggerContext context, String moduleName, Logger logger) {
        // Crear appender específico para el módulo
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setName("MODULE_" + moduleName.toUpperCase());
        appender.setFile("./logs/" + moduleName + ".json");

        // Configurar encoder JSON
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setCustomFields("{\"service\":\"" + getApplicationName() + "\",\"module\":\"" + moduleName + "\"}");
        appender.setEncoder(encoder);

        // Configurar rolling policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern("./logs/archived/" + moduleName + "-%d{yyyy-MM-dd}-%i.json");
        rollingPolicy.setMaxFileSize(FileSize.valueOf("50MB"));
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setTotalSizeCap(FileSize.valueOf("2GB"));
        rollingPolicy.setParent(appender);
        appender.setRollingPolicy(rollingPolicy);

        appender.start();
        logger.addAppender(appender);

        // Usar nivel desde configuración
        logger.setLevel(Level.valueOf(defaultLevel));
        logger.setAdditive(false);

        log.info("✅ Logger configurado para módulo: {} con nivel: {}", moduleName, defaultLevel);
    }

    private String getApplicationName() {
        return environment.getProperty("spring.application.name", "walrex-monolith");
    }
}
