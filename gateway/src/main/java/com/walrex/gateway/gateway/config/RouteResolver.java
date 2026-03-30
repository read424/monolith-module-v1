package com.walrex.gateway.gateway.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RouteResolver {

    private final ModulesUrlRepository modulesUrlRepository;
    private final MeterRegistry meterRegistry;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    private final Counter cacheHitExact;
    private final Counter cacheHitPattern;
    private final Counter cacheMiss;
    private final Timer dbLookupTimer;

    public RouteResolver(ModulesUrlRepository modulesUrlRepository, MeterRegistry meterRegistry) {
        this.modulesUrlRepository = modulesUrlRepository;
        this.meterRegistry = meterRegistry;
        this.cacheHitExact    = Counter.builder("gateway.route.cache.hit").tag("type", "exact").register(meterRegistry);
        this.cacheHitPattern  = Counter.builder("gateway.route.cache.hit").tag("type", "pattern").register(meterRegistry);
        this.cacheMiss        = Counter.builder("gateway.route.cache.miss").register(meterRegistry);
        this.dbLookupTimer    = Timer.builder("gateway.route.db.lookup").register(meterRegistry);
    }

    private final ConcurrentHashMap<String, ModulesUrl> exactPathCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ModulesUrl> patternPathCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Pattern> compiledRegexCache = new ConcurrentHashMap<>();
    private final AtomicLong lastCacheRefresh = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean refreshing = new AtomicBoolean(false);
    private static final long CACHE_TTL = 60_000;

    public Mono<ModulesUrl> resolve(String requestPath) {
        if (System.currentTimeMillis() - lastCacheRefresh.get() > CACHE_TTL
                && refreshing.compareAndSet(false, true)) {
            refreshCache();
        }

        ModulesUrl exact = exactPathCache.get(requestPath);
        if (exact != null) {
            log.debug("Módulo encontrado en caché (exacta): {}", requestPath);
            cacheHitExact.increment();
            return Mono.just(exact);
        }

        ModulesUrl pattern = findPatternMatchInCache(requestPath);
        if (pattern != null) {
            log.debug("Módulo encontrado en caché (patrón): {}", requestPath);
            cacheHitPattern.increment();
            return Mono.just(pattern);
        }

        cacheMiss.increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        return modulesUrlRepository.findAll()
            .filter(module -> {
                if (isExactMatch(requestPath, module)) {
                    log.info("Coincidencia EXACTA: '{}' -> '{}'", requestPath, module.getPath());
                    return true;
                }
                if (isSpringPatternMatch(requestPath, module)) {
                    log.info("Coincidencia PATRÓN SPRING: '{}' -> '{}'", requestPath, module.getPath());
                    return true;
                }
                if (isRegexMatch(requestPath, module)) {
                    log.info("Coincidencia REGEX: '{}' -> '{}'", requestPath, module.getPath());
                    return true;
                }
                return false;
            })
            .next()
            .doOnNext(module -> {
                sample.stop(dbLookupTimer);
                if (Boolean.TRUE.equals(module.getIsPattern())) {
                    patternPathCache.put(module.getPath(), module);
                } else if (module.getPath().contains("*")) {
                    patternPathCache.put(module.getPath(), module);
                } else {
                    exactPathCache.put(requestPath, module);
                }
            })
            .doOnSuccess(m -> { if (m == null) sample.stop(dbLookupTimer); });
    }

    public Mono<ModulesUrl> findModuleByPattern(String requestPath) {
        return modulesUrlRepository.findAll()
            .filter(module -> {
                if (module.getIsPattern() == null || !module.getIsPattern()) return false;
                if (module.getPath() == null || module.getPath().isEmpty()) return false;
                return evaluateRegexPattern(module, requestPath);
            })
            .next();
    }

    private ModulesUrl findPatternMatchInCache(String requestPath) {
        for (String patternStr : patternPathCache.keySet()) {
            try {
                ModulesUrl module = patternPathCache.get(patternStr);
                if (Boolean.TRUE.equals(module.getIsPattern())) {
                    Pattern compiled = getCompiledRegex(patternStr);
                    if (compiled != null && compiled.matcher(requestPath).matches()) return module;
                } else {
                    PathPattern pp = pathPatternParser.parse(patternStr);
                    if (pp.matches(PathContainer.parsePath(requestPath))) return module;
                }
            } catch (Exception e) {
                log.warn("Error evaluando patrón '{}' contra '{}': {}", patternStr, requestPath, e.getMessage());
            }
        }
        return null;
    }

    private void refreshCache() {
        modulesUrlRepository.findAll()
            .collectList()
            .subscribe(
                modules -> {
                    exactPathCache.clear();
                    patternPathCache.clear();
                    compiledRegexCache.clear();
                    for (ModulesUrl module : modules) {
                        if (module.getPath() == null || module.getPath().isEmpty()) continue;
                        if (Boolean.TRUE.equals(module.getIsPattern())) {
                            patternPathCache.put(module.getPath(), module);
                            getCompiledRegex(module.getPath());
                        } else if (module.getPath().contains("*")) {
                            patternPathCache.put(module.getPath(), module);
                        } else {
                            exactPathCache.put(module.getPath(), module);
                        }
                    }
                    lastCacheRefresh.set(System.currentTimeMillis());
                    log.info("Caché refrescado: {} exactas, {} patrones", exactPathCache.size(), patternPathCache.size());
                    refreshing.set(false);
                },
                error -> {
                    log.error("Error al refrescar caché de rutas: {}", error.getMessage());
                    refreshing.set(false); // libera para que el próximo request reintente
                }
            );
    }

    private boolean isExactMatch(String requestPath, ModulesUrl module) {
        if (module.getIsPattern() != null && module.getIsPattern()) return false;
        return requestPath.equals(module.getPath());
    }

    private boolean isSpringPatternMatch(String requestPath, ModulesUrl module) {
        // isPattern=true significa regex explícito → lo maneja isRegexMatch
        if (Boolean.TRUE.equals(module.getIsPattern())) return false;
        if (module.getPath() == null || module.getPath().isEmpty() || !module.getPath().contains("*")) return false;
        try {
            PathPattern pattern = pathPatternParser.parse(module.getPath());
            return pattern.matches(PathContainer.parsePath(requestPath));
        } catch (Exception e) {
            log.warn("Error evaluando patrón Spring '{}': {}", module.getPath(), e.getMessage());
            return false;
        }
    }

    private boolean isRegexMatch(String requestPath, ModulesUrl module) {
        // Solo aplica a rutas marcadas explícitamente como regex (isPattern=true)
        if (!Boolean.TRUE.equals(module.getIsPattern())) return false;
        if (module.getPath() == null || module.getPath().isEmpty()) return false;
        try {
            Pattern compiled = getCompiledRegex(module.getPath());
            return compiled != null && compiled.matcher(requestPath).matches();
        } catch (Exception e) {
            log.warn("Error evaluando regex '{}': {}", module.getPath(), e.getMessage());
            return false;
        }
    }

    private boolean evaluateRegexPattern(ModulesUrl module, String requestPath) {
        try {
            Pattern compiled = getCompiledRegex(module.getPath());
            return compiled != null && compiled.matcher(requestPath).matches();
        } catch (Exception e) {
            log.warn("Error evaluando regex '{}' para '{}': {}", module.getPath(), requestPath, e.getMessage());
            return false;
        }
    }

    private Pattern getCompiledRegex(String regex) {
        return compiledRegexCache.computeIfAbsent(regex, r -> {
            try {
                return Pattern.compile(r);
            } catch (Exception e) {
                log.error("Error compilando regex '{}': {}", r, e.getMessage());
                return null;
            }
        });
    }
}
