package com.walrex.gateway.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DynamicModuleRouteFilter extends AbstractGatewayFilterFactory<DynamicModuleRouteFilter.Config> {
    private final ModulesUrlRepository modulesUrlRepository;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    // Caché para mejorar el rendimiento
    private final ConcurrentHashMap<String, ModulesUrl> exactPathCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ModulesUrl> patternPathCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Pattern> compiledRegexCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PathPattern> compiledPathPatternCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, URI> lbUriCache = new ConcurrentHashMap<>();
    private final AtomicLong lastCacheRefresh = new AtomicLong(System.currentTimeMillis());
    private final long CACHE_TTL = 60000; // 1 minuto

    // 🔥 CONSTANTE PARA MÁXIMO DE FORWARDS
    private static final int MAX_FORWARD_COUNT = 2;
    private static final String FORWARD_COUNT_KEY = "FORWARD_COUNT";
    private static final String DYNAMIC_MODULE_ROUTE_PROCESSED = "DYNAMIC_MODULE_ROUTE_PROCESSED";
    private static final String GATEWAY_FORWARDED_REQUEST = "GATEWAY_FORWARDED_REQUEST";

    public DynamicModuleRouteFilter(
        ModulesUrlRepository modulesUrlRepository
    ) {
        super(Config.class);
        this.modulesUrlRepository = modulesUrlRepository;
    }

    @Override
    public GatewayFilter apply(Config config){
        return new OrderedGatewayFilter((exchange, chain) -> {
            Integer loopCounter = exchange.getAttributeOrDefault("LOOP_COUNTER", 0);
            loopCounter++;
            exchange.getAttributes().put("LOOP_COUNTER", loopCounter);

            String path = exchange.getRequest().getPath().value();
            log.debug("DynamicModuleRouteFilter - Path: '{}'", path);

            URI gatewayUrlAttr = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            // ✅ Verificar si es una petición forward para evitar bucles
            Boolean forwarded = exchange.getAttribute(GATEWAY_FORWARDED_REQUEST);
            if ((forwarded != null && forwarded)) {
                log.debug("✅ [Loop#{}] Request already forwarded -> SKIP", loopCounter);
                return chain.filter(exchange);
            }

            Boolean processed = exchange.getAttribute(DYNAMIC_MODULE_ROUTE_PROCESSED);
            if (processed != null && processed) {
                log.debug("✅ [Loop#{}] Already processed by dynamic route filter -> SKIP", loopCounter);
                if (loopCounter > 5) {
                    log.error("❌ [Loop#{}] LOOP DETECTION: Demasiadas iteraciones para path: {} processed", loopCounter, path);
                    exchange.getResponse().setStatusCode(HttpStatus.LOOP_DETECTED);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
            log.debug("[Loop#{}] DynamicModuleRouteFilter - path='{}' gatewayUrlAttr={} forwarded={} processed={}",
                loopCounter, path, gatewayUrlAttr, forwarded, processed);

            // ✅ PROTECCIÓN ADICIONAL: Contador de loops
            if (loopCounter > 5) {
                log.error("❌ [Loop#{}] LOOP DETECTION: Demasiadas iteraciones para path: {}", loopCounter, path);
                exchange.getResponse().setStatusCode(HttpStatus.LOOP_DETECTED);
                return exchange.getResponse().setComplete();
            }
            // Refresh cache periodically
            if (System.currentTimeMillis() - lastCacheRefresh.get() > CACHE_TTL) {
                refreshCache();
            }

            final ServerHttpRequest request = exchange.getRequest();
            final String requestPath;
            if (exchange.getAttribute("ORIGINAL_PATH") != null) {
                requestPath = exchange.getAttribute("ORIGINAL_PATH");
            } else {
                requestPath = request.getPath().value();
            }

            ModulesUrl exact = exactPathCache.get(requestPath);
            if (exact != null) {
                log.debug("Found exact cache for '{}'", requestPath);
                return processRouting(exact, requestPath, request, exchange, chain);
            }

            ModulesUrl cachedPattern = findPatternMatchInCache(requestPath);
            if (cachedPattern != null) {
                log.debug("Found pattern cache for '{}'", requestPath);
                return processRouting(cachedPattern, requestPath, request, exchange, chain);
            }

            return modulesUrlRepository.findAll()
                .filter(module -> matchesModule(requestPath, module))
                .next()
                .doOnNext(module -> {
                    // cache according to type
                    if (isRegexPattern(module.getPath())) {
                        patternPathCache.put(module.getPath(), module);
                        getCompiledRegex(module.getPath());
                    } else if (isSpringPattern(module.getPath())) {
                        patternPathCache.put(module.getPath(), module);
                    } else {
                        exactPathCache.put(module.getPath(), module);
                    }
                    log.debug("Selected module {} for path {}", module.getModuleName(), requestPath);
                })
                .flatMap(module -> processRouting(module, requestPath, request, exchange, chain))
                .switchIfEmpty(Mono.defer(() -> {
                    // If a GATEWAY_REQUEST_URL_ATTR was already set by other logic, don't 404
                    URI configuredUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                    if (configuredUri != null) {
                        log.debug("GATEWAY_REQUEST_URL_ATTR already present ({}). Letting chain continue.", configuredUri);
                        return chain.filter(exchange);
                    }
                    log.warn("No module configuration found for path: {}", requestPath);
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    return exchange.getResponse().setComplete();
                }));
        }, 9500);
    }

    /**
     * 🔍 Busca en el caché de patrones un módulo que coincida con la ruta solicitada
     * Optimizado: usa patrones pre-compilados para mejor performance
     */
    private ModulesUrl findPatternMatchInCache(String requestPath) {
        log.debug("🔍 Buscando en caché de patrones para: '{}'", requestPath);

        PathContainer pathContainer = PathContainer.parsePath(requestPath);

        for (var entry : patternPathCache.entrySet()) {
            String patternStr = entry.getKey();
            ModulesUrl module = entry.getValue();

            try {
                if (module.getIsPattern() != null && module.getIsPattern()) {
                    // 🎯 Usar regex compilado para isPattern = true
                    Pattern compiledPattern = getCompiledRegex(patternStr);
                    if (compiledPattern != null && compiledPattern.matcher(requestPath).matches()) {
                        log.debug("✅ [Cache-Regex] COINCIDENCIA: '{}' -> '{}'", requestPath, patternStr);
                        return module;
                    }
                } else {
                    // 🎯 Usar PathPattern pre-compilado de Spring
                    PathPattern pattern = getCompiledPathPattern(patternStr);
                    if (pattern != null && pattern.matches(pathContainer)) {
                        log.debug("✅ [Cache-Spring] COINCIDENCIA: '{}' -> '{}'", requestPath, patternStr);
                        return module;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Error evaluando patrón '{}' contra ruta '{}': {}", patternStr, requestPath, e.getMessage());
            }
        }

        log.debug("🔍 No se encontraron coincidencias en caché de patrones");
        return null;
    }

    /**
     * 📝 Obtiene o compila un PathPattern de Spring con caché
     */
    private PathPattern getCompiledPathPattern(String pattern) {
        return compiledPathPatternCache.computeIfAbsent(pattern, p -> {
            try {
                PathPattern compiled = pathPatternParser.parse(p);
                log.debug("PathPattern compilado: '{}'", p);
                return compiled;
            } catch (Exception e) {
                log.error("Error compilando PathPattern '{}': {}", p, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Procesa el enrutamiento basado en el módulo encontrado
     *
     * NUEVO: Integra Consul Service Discovery para resolver servicios externos
     * - Si module.uri es null o "monolito-modular" → Forward interno (actual)
     * - Si module.uri es un service_name → Consulta Consul y hace proxy externo
     */
    protected Mono<Void> processRouting(ModulesUrl module,
                                        String requestPath,
                                        ServerHttpRequest request,
                                        ServerWebExchange exchange,
                                        GatewayFilterChain chain) {
        String serviceUri = module.getUri();

        // 1) Internal forward (same app / monolito)
        if (serviceUri == null || serviceUri.isBlank() || "monolito-modular".equalsIgnoreCase(serviceUri)) {
            log.debug("-> Internal forward (monolito) for module {}", module.getModuleName());
            return processRoutingInternal(module, requestPath, request, exchange, chain);
        }

        if (serviceUri.startsWith("lb://")) {
            log.debug("-> LoadBalancer routing for '{}'", serviceUri);
            return processRoutingWithLoadBalancer(module, requestPath, serviceUri, exchange, chain);
        }

        log.debug("-> Treating '{}' as external host, prefixing http://", serviceUri);
        return processRoutingExternal(module, requestPath, serviceUri, exchange, chain);
    }

    /**
     * 🔧 Routing INTERNO para monolito (forward:/)
     * Mantiene la lógica original sin cambios
     */
    private Mono<Void> processRoutingInternal(ModulesUrl module,
                                              String requestPath,
                                              ServerHttpRequest request,
                                              ServerWebExchange exchange,
                                              GatewayFilterChain chain) {
        //Incrementar contador de forwards
        Integer forwardCount = exchange.getAttributeOrDefault(FORWARD_COUNT_KEY, 0);
        if (forwardCount >= MAX_FORWARD_COUNT) {
            log.error("Forward limit exceeded ({}) for path {}", MAX_FORWARD_COUNT, requestPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        exchange.getAttributes().put(FORWARD_COUNT_KEY, forwardCount + 1);

        String newPath = processPath(requestPath, module);
        if (newPath == null || newPath.isBlank()) {
            log.error("Invalid newPath after processPath (original: {})", requestPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        if (newPath.equals(requestPath)) {
            log.error("Detected forwarding loop: newPath == requestPath ({})", newPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        log.info("Forwarding internally: {} -> {} (forward #{})", requestPath, newPath, forwardCount + 1);

        // mark attributes minimally
        exchange.getAttributes().put(GATEWAY_FORWARDED_REQUEST, true);
        exchange.getAttributes().put(DYNAMIC_MODULE_ROUTE_PROCESSED, true);
        if (exchange.getAttribute("ORIGINAL_PATH") == null) {
            exchange.getAttributes().put("ORIGINAL_PATH", requestPath);
        }

        // prepare forward URI with query string preserved
        String rawQuery = request.getURI().getRawQuery();
        String forwardUriString = "forward:" + newPath + (rawQuery != null && !rawQuery.isEmpty() ? "?" + rawQuery : "");
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(forwardUriString));

        Mono<byte[]> cachedBody = DataBufferUtils.join(exchange.getRequest().getBody())
            .map(dataBuffer -> {
                try {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                } finally {
                    DataBufferUtils.release(dataBuffer);
                }
            })
            .defaultIfEmpty(new byte[0])
            .cache();

        // build base mutated request (path only)
        ServerHttpRequest baseMutatedRequest = request.mutate()
            .path(newPath.contains("?") ? newPath.substring(0, newPath.indexOf("?")) : newPath)
            .build();

        // create decorator that will re-inject the cached body when requested by handlers
        Mono<ServerHttpRequest> mutatedRequestMono = cachedBody.map(bytes -> {
            if (bytes.length == 0) {
                return baseMutatedRequest;
            }
            return new ServerHttpRequestDecorator(baseMutatedRequest) {
                @Override
                public Flux<DataBuffer> getBody() {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    DataBuffer buffer = bufferFactory.wrap(bytes);
                    return Flux.just(buffer);
                }
            };
        });

        return mutatedRequestMono.flatMap(mutReq -> {
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutReq).build();

            // transfer a minimal safe set of attributes
            mutatedExchange.getAttributes().put(FORWARD_COUNT_KEY, exchange.getAttribute(FORWARD_COUNT_KEY));
            mutatedExchange.getAttributes().put(GATEWAY_FORWARDED_REQUEST, true);
            mutatedExchange.getAttributes().put("ORIGINAL_PATH", exchange.getAttribute("ORIGINAL_PATH"));
            mutatedExchange.getAttributes().put(DYNAMIC_MODULE_ROUTE_PROCESSED, true);

            return chain.filter(mutatedExchange)
                .doOnSuccess(v -> log.info("Internal forward completed: {} -> {}", requestPath, newPath))
                .doOnError(e -> log.error("Error on internal forward {} -> {} : {}", requestPath, newPath, e.getMessage()));
        });
    }

    private Mono<Void> processRoutingWithLoadBalancer(ModulesUrl module,
                                                      String requestPath,
                                                      String lbUri,
                                                      ServerWebExchange exchange,
                                                      GatewayFilterChain chain) {

        String newPath = processPath(requestPath, module);

        String fullUri = lbUri + newPath;
        String queryString = exchange.getRequest().getURI().getRawQuery();
        if (queryString != null && !queryString.isEmpty()) {
            fullUri += "?" + queryString;
        }

        log.info("LoadBalancer routing: {} -> {}", requestPath, fullUri);

        // Usar caché para la URI del LoadBalancer
        URI loadBalancedUri = getCachedLbUri(fullUri);
        if (loadBalancedUri == null) {
            log.error("Invalid lb URI: {}", fullUri);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
            return exchange.getResponse().setComplete();
        }

        Route originalRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (originalRoute == null) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        Route newRoute = Route.async()
                .id(originalRoute.getId())
                .uri(loadBalancedUri)
                .order(originalRoute.getOrder())
                .asyncPredicate(originalRoute.getPredicate())
                .metadata(originalRoute.getMetadata())
                .build();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .path(newPath)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Poner atributos en mutatedExchange (no en exchange original)
        mutatedExchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, loadBalancedUri);
        mutatedExchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
        mutatedExchange.getAttributes().put(DYNAMIC_MODULE_ROUTE_PROCESSED, true);

        return chain.filter(mutatedExchange)
                .doOnSuccess(v -> log.debug("LoadBalancer proxy completed for {}", loadBalancedUri))
                .doOnError(e -> log.error("Error during LoadBalancer proxy {} : {}", loadBalancedUri, e.getMessage()));
    }

    /**
     * Obtiene o crea una URI cacheada para LoadBalancer
     */
    private URI getCachedLbUri(String uriString) {
        return lbUriCache.computeIfAbsent(uriString, s -> {
            try {
                URI uri = new URI(s);
                log.debug("URI LoadBalancer cacheada: '{}'", s);
                return uri;
            } catch (URISyntaxException e) {
                log.error("Error creando URI '{}': {}", s, e.getMessage());
                return null;
            }
        });
    }

    private boolean matchesModule(String requestPath, ModulesUrl module) {
        String pattern = module.getPath();
        if (pattern == null) return false;

        // If the module explicitly set isPattern flag and you want to rely on it, you can,
        // but we implement robust auto-detection:
        if (isRegexPattern(pattern)) {
            return isRegexMatch(requestPath, module);
        } else if (isSpringPattern(pattern)) {
            return isSpringPatternMatch(requestPath, module);
        } else {
            return isExactMatch(requestPath, module);
        }
    }

    private boolean isRegexPattern(String path) {
        if (path == null) return false;
        // consider typical regex meta-characters
        return path.matches(".*[\\^$+()\\\\\\[\\]|].*");
    }

    private boolean isSpringPattern(String path) {
        if (path == null) return false;
        return path.contains("*") || path.contains("{");
    }


    /**
     * Encuentra un módulo que coincida con un patrón para la ruta dada
     */
    protected Mono<ModulesUrl> findModuleByPattern(String requestPath) {
        log.info("🔍 [findModuleByPattern] Buscando patrón REGEX para ruta: '{}'", requestPath);

        return modulesUrlRepository.findAll()
                .filter(module -> {
                    // Solo procesar módulos con isPattern = true
                    if (module.getIsPattern() == null || !module.getIsPattern()) {
                        return false;
                    }

                    if (module.getPath() == null || module.getPath().isEmpty()) {
                        log.debug("🚫 Módulo '{}' descartado - path vacío", module.getModuleName());
                        return false;
                    }

                    // ✅ Usar solo REGEX para isPattern = true
                    return evaluateRegexPattern(module, requestPath);
                })
                .next() // ⚡ IMPORTANTE: Tomar solo el PRIMER resultado
                .doOnNext(module -> {
                    log.info("✅ [findModuleByPattern] PRIMERA COINCIDENCIA encontrada: '{}' -> '{}'",
                            requestPath, module.getPath());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("🔍 [findModuleByPattern] No se encontraron patrones REGEX para: '{}'", requestPath);
                    return Mono.empty();
                }));
    }

    /**
     * Extrae la parte de la ruta después del patrón configurado
     */
    protected String processPath(String requestPath, ModulesUrl module) {
        log.debug("🔧 processPath - Input: '{}', stripCount: {}", requestPath, module.getStripPrefixCount());

        if (module.getStripPrefixCount() != null && module.getStripPrefixCount() > 0) {
            String[] segments = requestPath.split("/");
            log.debug("🔧 processPath - Segments: {}", Arrays.toString(segments));

            StringBuilder strippedPath = new StringBuilder();
            int segmentsToSkip = module.getStripPrefixCount();

            // Construir la nueva ruta omitiendo los segmentos indicados
            for (int i = segmentsToSkip + 1; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    strippedPath.append("/").append(segments[i]);
                }
            }

            String result = strippedPath.length() > 0 ? strippedPath.toString() : "/";
            log.debug("🔧 processPath - Output: '{}'", result);
            return result   ;
        }
        // Si no hay stripPrefix, usar la ruta completa
        return requestPath;
    }

    /**
     * Refresca el caché de rutas desde la base de datos
     */
    private void refreshCache() {
        log.info("🔄 Refrescando caché de rutas...");
        modulesUrlRepository.findAll()
                .collectList()
                .subscribe(modules -> {
                    exactPathCache.clear();
                    patternPathCache.clear();
                    compiledRegexCache.clear();
                    compiledPathPatternCache.clear();
                    lbUriCache.clear();

                    for (ModulesUrl module : modules) {
                        if (module.getPath() != null && !module.getPath().isEmpty()) {
                            if (module.getIsPattern() != null && module.getIsPattern()) {
                                // Es un patrón regex
                                patternPathCache.put(module.getPath(), module);
                                // Pre-compilar regex para performance
                                getCompiledRegex(module.getPath());
                                log.debug("📋 Cacheado patrón REGEX: '{}'", module.getPath());
                            } else if (module.getPath().contains("*") || module.getPath().contains("{")) {
                                // Es un patrón de Spring
                                patternPathCache.put(module.getPath(), module);
                                // Pre-compilar PathPattern para performance
                                getCompiledPathPattern(module.getPath());
                                log.debug("📋 Cacheado patrón SPRING: '{}'", module.getPath());
                            } else {
                                // Es una ruta exacta
                                exactPathCache.put(module.getPath(), module);
                                log.debug("📋 Cacheada ruta EXACTA: '{}'", module.getPath());
                            }
                        }
                    }

                    lastCacheRefresh.set(System.currentTimeMillis());
                    log.info("✅ Caché refrescado: {} rutas exactas, {} patrones, {} regex, {} pathPatterns",
                            exactPathCache.size(), patternPathCache.size(), compiledRegexCache.size(), compiledPathPatternCache.size());
                });
    }

    /**
     * 🎯 Evalúa patrones usando expresiones regulares (solo para isPattern = true)
     */
    private boolean evaluateRegexPattern(ModulesUrl module, String requestPath) {
        try {
            Pattern compiledPattern = getCompiledRegex(module.getPath());
            if (compiledPattern != null) {
                boolean matches = compiledPattern.matcher(requestPath).matches();

                if (matches) {
                    log.info("✅ [REGEX] COINCIDENCIA: '{}' matches '{}' ({})",
                            requestPath, module.getPath(), module.getModuleName());
                    return true;
                } else {
                    log.debug("🔍 [REGEX] NO COINCIDE: '{}' vs '{}' ({})",
                            requestPath, module.getPath(), module.getModuleName());
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ [REGEX] Error evaluando '{}' para '{}': {}",
                    module.getPath(), requestPath, e.getMessage());
        }
        return false;
    }

    /**
     * 🔍 Verifica si la ruta es una coincidencia exacta
     */
    private boolean isExactMatch(String requestPath, ModulesUrl module) {
        if (module.getIsPattern() != null && module.getIsPattern()) {
            return false;
        }
        return requestPath.equals(module.getPath());
    }

    /**
     * 🔍 Verifica si hay coincidencia con patrón Spring (contiene *)
     */
    private boolean isSpringPatternMatch(String requestPath, ModulesUrl module) {
        if (module.getIsPattern() != null && !module.getIsPattern()) {
            return false;// No se debe hacer match por patrón
        }
        if(module.getPath() == null || module.getPath().isEmpty() || !module.getPath().contains("*")) {
            return false;
        }
        try{
            PathPattern pattern = pathPatternParser.parse(module.getPath());
            return pattern.matches(PathContainer.parsePath(requestPath));
        }catch (Exception e){
            log.warn("Error evaluando patrón Spring '{}': {}", module.getPath(), e.getMessage());
            return false;
        }
    }

    /**
     * 🔍 Verifica si hay coincidencia con patrón REGEX
     */
    private boolean isRegexMatch(String requestPath, ModulesUrl module) {
        if (module.getIsPattern() != null && !module.getIsPattern()) {
            return false;// No es un patrón regex
        }
        if(module.getPath() == null || module.getPath().isEmpty()) {
            return false;
        }
        try{
            Pattern compiledPattern = getCompiledRegex(module.getPath());
            return compiledPattern != null && compiledPattern.matcher(requestPath).matches();
        }catch(Exception e){
            log.warn("Error evaluando regex '{}': {}", module.getPath(), e.getMessage());
            return false;
        }
    }

    /**
     * 📝 Obtiene o compila un patrón regex con caché
     */
    private Pattern getCompiledRegex(String regex) {
        return compiledRegexCache.computeIfAbsent(regex, r -> {
            try {
                Pattern pattern = Pattern.compile(r);
                log.debug("Regex compilado: '{}'", r);
                return pattern;
            } catch (Exception e) {
                log.error("Error compilando regex '{}': {}", r, e.getMessage());
                return null;
            }
        });
    }

    /**
     * 🆕 Routing EXTERNO para microservicios (http://IP:PORT)
     *
     * Delega el proxying a Spring Cloud Gateway que maneja automáticamente:
     * - Headers (preserva, elimina hop-by-hop)
     * - Body (streaming reactivo)
     * - Conexiones (pooling con Netty)
     * - Timeouts y Circuit Breakers
     */
    private Mono<Void> processRoutingExternal(ModulesUrl module, String requestPath,
                                              String serviceUri,
                                              ServerWebExchange exchange,
                                              GatewayFilterChain chain) {
        String newPath = processPath(requestPath, module);
        String fullUrl = serviceUri + newPath;

        log.info("🌐 Proxy externo: '{}' → '{}'", requestPath, fullUrl);

        // Agregar query string si existe
        String queryString = exchange.getRequest().getURI().getRawQuery();
        if (queryString != null && !queryString.isEmpty()) {
            fullUrl += "?" + queryString;
        }

        try {
            URI externalUri = new URI(fullUrl);

            // Spring Cloud Gateway maneja automáticamente el proxying
            exchange.getAttributes().put(
                ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                externalUri
            );

            log.debug("✅ URI externa configurada: {}", externalUri);

            final String finalFullUrl = fullUrl;
            return chain.filter(exchange)
                .doOnSuccess(v -> log.info("✅ Proxy externo completado: {}", finalFullUrl))
                .doOnError(e -> log.error("❌ Error en proxy externo '{}': {}", finalFullUrl, e.getMessage()));

        } catch (URISyntaxException e) {
            log.error("❌ URI inválida: {}", fullUrl, e);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            String errorBody = String.format(
                "{\"error\":\"Bad Gateway\",\"message\":\"URI inválida para servicio externo: %s\",\"path\":\"%s\"}",
                fullUrl, requestPath);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    public static class Config {
        // Puedes agregar configuración si es necesario
    }
}
