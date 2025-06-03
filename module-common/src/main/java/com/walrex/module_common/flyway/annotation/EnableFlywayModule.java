package com.walrex.module_common.flyway.annotation;

import com.walrex.module_common.config.CommonConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CommonConfiguration.class)
public @interface EnableFlywayModule {
}
