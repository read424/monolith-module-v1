package com.walrex.module_articulos.common;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

//@Aspect
//@Component
@Slf4j
public class QueryParameterLogger {

    //@Around("@annotation(org.springframework.data.r2dbc.repository.Query)")
    public Object logQueryParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("Método: {}, Parámetros: {}", methodName, Arrays.toString(args));
        return joinPoint.proceed();
    }
}
