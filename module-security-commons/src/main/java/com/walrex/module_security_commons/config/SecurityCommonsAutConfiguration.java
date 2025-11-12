package com.walrex.module_security_commons.config;

import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.infrastructure.adapters.JwtUserContextService;
import com.walrex.module_security_commons.infrastructure.aspects.PermissionValidationAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
* Auto-configuración de Spring Boot para Security Commons
*
* Registra automáticamente:
* - JwtUserContextService: Extracción de usuario desde headers
* - PermissionValidationAspect: Validación declarativa de permisos
*/
@Configuration
@EnableAspectJAutoProxy
@Slf4j
public class SecurityCommonsAutConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserContextProvider userContextProvider(){
        log.info("Registrando JwtUserContextService");
        return new JwtUserContextService();
    }

    public PermissionValidationAspect permissionValidationAspect(
        UserContextProvider userContextProvider
    ){
        log.info("Registrando PermissionValidationAspect");
        return new PermissionValidationAspect(userContextProvider);
    }
}
