package com.walrex.module_security_commons.infrastructure.aspects;

import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.domain.exceptions.ForbiddenException;
import com.walrex.module_security_commons.domain.exceptions.UnauthorizedException;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Arrays;

/**
 * Aspecto AOP para validación automática de permisos
 *
 * Intercepta métodos anotados con:
 * - @RequiresPermission: Valida un permiso específico
 * - @RequiresAnyPermission: Valida al menos uno de los permisos
 *
 * Si la validación falla, lanza:
 * - UnauthorizedException: Si no hay usuario autenticado
 * - ForbiddenException: Si no tiene el permiso requerido
 *
 * @author Security Commons Module
 */

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionValidationAspect {
    private final UserContextProvider userContextProvider;

    @Around("@annotation(requiresPermission)")
    public Object validatePermission(
        ProceedingJoinPoint joinPoint,
        RequiresPermission requiresPermission) throws Throwable {
            log.debug("Validando permiso: {}", requiresPermission.value());

            ServerRequest request = extractServerRequest(joinPoint);

        JwtUserInfo user = userContextProvider.getCurrentUser(request);

        if(!user.isAuthenticated()){
            log.warn("Usuario no autenticado intentando acceder a {}", joinPoint.getSignature().getName());
            throw new UnauthorizedException("Usuario no autenticado");
        }

        if(!user.hasPermission(requiresPermission.value())){
            log.warn("Usuario '{}' sin permiso '{}' para {}", user.getUsername(), requiresPermission.value(), joinPoint.getSignature().getName());
            throw new ForbiddenException(requiresPermission.message(), requiresPermission.value());
        }

        log.debug("Usuario '{}' tiene permiso '{}'", user.getUsername(), requiresPermission.value());
        return joinPoint.proceed();
    }

    /**
     * Intercepta metodos con @RequiresAnyPermission
     */
    @Around("@annotation(requiresAnyPermission)")
    public Object validateAnyPermission(
            ProceedingJoinPoint joinPoint,
            RequiresAnyPermission requiresAnyPermission) throws Throwable  {
        log.debug("Validando permisos (al menos uno): {}", Arrays.toString(requiresAnyPermission.value()));

        ServerRequest request = extractServerRequest(joinPoint);

        JwtUserInfo user = userContextProvider.getCurrentUser(request);

        if(!user.isAuthenticated()){
            log.warn("Usuario no autenticado intentado acceder a {}", joinPoint.getSignature().getName());
            throw new UnauthorizedException("Usuario no autenticado");
        }

        if(!user.hasAnyPermission(requiresAnyPermission.value())){
            log.warn("Usuario '{}' sin ningun de los permisos {} para {}", user.getUsername(), Arrays.toString(requiresAnyPermission.value()),
                joinPoint.getSignature().getName());
            throw new ForbiddenException(
                requiresAnyPermission.message(),
                Arrays.toString(requiresAnyPermission.value())
            );
        }

        log.debug("Usuario '{}' tiene al menos uno de los permisos {}", user.getUsername(), Arrays.toString(requiresAnyPermission.value()));

        return joinPoint.proceed();
    }

    /**
    * Extrae ServerRequest del primer argumento del método
    */
    private ServerRequest extractServerRequest(ProceedingJoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();

        if(args.length == 0 ){
            throw new IllegalStateException("El método anotado debe recibir ServerRequest como primer parámetro");
        }

        if(!(args[0] instanceof ServerRequest)){
            throw new IllegalStateException("El primer parámetro debe ser ServerRequest, pero es: "+ args[0].getClass().getName());
        }

        return (ServerRequest) args[0];
    }
}
