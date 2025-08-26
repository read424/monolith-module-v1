package com.walrex.gateway.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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
    private final AtomicLong lastCacheRefresh = new AtomicLong(System.currentTimeMillis());
    private final long CACHE_TTL = 60000; // 1 minuto

    // 🔥 CONSTANTE PARA MÁXIMO DE FORWARDS
    private static final int MAX_FORWARD_COUNT = 2;
    private static final String FORWARD_COUNT_KEY = "FORWARD_COUNT";

    public DynamicModuleRouteFilter(ModulesUrlRepository modulesUrlRepository) {
        super(Config.class);
        this.modulesUrlRepository = modulesUrlRepository;
    }

    @Override
    public GatewayFilter apply(Config config){
        return ((exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String threadName = Thread.currentThread().getName();
            log.error("🟣 [5] DynamicModuleRouteFilter [{}] - Path: '{}'", threadName, path);

            // ✅ Verificar si es una petición forward para evitar bucles
            Boolean isForwarded = exchange.getAttribute("GATEWAY_FORWARDED_REQUEST");
            if (isForwarded != null && isForwarded) {
                log.debug("Petición ya forwardeada, saltando DynamicModuleRouteFilter");
                return chain.filter(exchange);
            }

            // Verificar si esta solicitud ya ha sido procesada por este filtro
            Boolean processed = exchange.getAttribute("DYNAMIC_MODULE_ROUTE_PROCESSED");
            if (processed != null && processed) {
                // Esta solicitud ya ha sido procesada, pasar al siguiente filtro
                return chain.filter(exchange);
            }

            // ✅ Control de contador de forwards por solicitud
            Integer forwardCount = exchange.getAttribute(FORWARD_COUNT_KEY);
            if (forwardCount == null) {
                forwardCount = 0;
            }

            // Marcar esta solicitud como procesada para evitar bucles
            exchange.getAttributes().put("DYNAMIC_MODULE_ROUTE_PROCESSED", true);

            // Obtener o inicializar el contador de redirecciones
            Integer redirectCount = exchange.getAttribute("REDIRECT_COUNT");
            if (redirectCount == null) {
                redirectCount = 0;
            }

            log.info("🔢 Forward Count actual: {}/{}", forwardCount, MAX_FORWARD_COUNT);

            if (forwardCount >= MAX_FORWARD_COUNT) {
                log.error("🚫 LÍMITE DE FORWARDS EXCEDIDO ({}/{}). Devolviendo 404", forwardCount, MAX_FORWARD_COUNT);
                exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                return exchange.getResponse().setComplete();
            }

            ServerHttpRequest request = exchange.getRequest();
            String requestPath;

            if (exchange.getAttribute("ORIGINAL_PATH") != null) {
                requestPath = exchange.getAttribute("ORIGINAL_PATH");
                log.info("📍 Usando ruta original desde atributo: {}", requestPath);
            } else {
                requestPath = request.getPath().value();
                log.info("📍 Usando ruta actual: {}", requestPath);
                // Si la ruta es solo "/", podría indicar que perdimos la ruta original
                if ("/".equals(requestPath)) {
                    log.warn("⚠️ Se recibió una ruta vacía (/), lo cual podría indicar un problema de redirección");
                }
            }

            if(System.currentTimeMillis()-lastCacheRefresh.get()>CACHE_TTL){
                refreshCache();
            }

            // 🎯 PASO 1: Búsqueda exacta en caché
            ModulesUrl cachedExactModule = exactPathCache.get(requestPath);
            if (cachedExactModule != null) {
                log.info("Módulo encontrado en caché (coincidencia exacta): {}", requestPath);
                return processRouting(cachedExactModule, requestPath, request, exchange, chain);
            }

            // 🎯 PASO 2: Búsqueda por patrón en caché
            ModulesUrl cachedPatternModule = findPatternMatchInCache(requestPath);
            if (cachedPatternModule != null) {
                log.info("✅ Módulo encontrado en caché (coincidencia de patrón): {}", requestPath);
                return processRouting(cachedPatternModule, requestPath, request, exchange, chain);
            }

            // 🎯 PASO 3: Búsqueda exacta en BD
            return modulesUrlRepository.findAll()
                .filter(module -> {
                    if(isExactMatch(requestPath, module)){
                        log.info("✅ Coincidencia EXACTA encontrada: '{}' -> '{}'", requestPath, module.getPath());
                        return true;
                    }
                    if (isSpringPatternMatch(requestPath, module)) {
                        log.info("✅ Coincidencia PATRÓN SPRING encontrada: '{}' -> '{}'", requestPath, module.getPath());
                        return true;
                    }

                    if (isRegexMatch(requestPath, module)) {
                        log.info("✅ Coincidencia REGEX encontrada: '{}' -> '{}'", requestPath, module.getPath());
                        return true;
                    }
                    return false;
                })
                .next() // ⚡ Tomar solo el PRIMER resultado que coincida
                .doOnNext(module -> {
                    log.info("🎯 Módulo seleccionado para routing: '{}' -> '{}'", requestPath, module.getPath());
                    // Cachear el resultado para futuras consultas
                    if (module.getIsPattern() != null && module.getIsPattern()) {
                        patternPathCache.put(module.getPath(), module);
                    } else if (module.getPath().contains("*")) {
                        patternPathCache.put(module.getPath(), module);
                    } else {
                        exactPathCache.put(requestPath, module);
                    }
                })
                .flatMap(module -> processRouting(module, requestPath, request, exchange, chain))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("❌ No se encontró configuración para la ruta: {}", requestPath);
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    return exchange.getResponse().setComplete();
                }));
        });
    }

    /**
     * 🔍 Busca en el caché de patrones un módulo que coincida con la ruta solicitada
     */
    private ModulesUrl findPatternMatchInCache(String requestPath) {
        log.debug("🔍 Buscando en caché de patrones para: '{}'", requestPath);
        
        for (String patternStr : patternPathCache.keySet()) {
            try {
                ModulesUrl module = patternPathCache.get(patternStr);
                
                if (module.getIsPattern() != null && module.getIsPattern()) {
                    // 🎯 Usar regex compilado para isPattern = true
                    Pattern compiledPattern = getCompiledRegex(patternStr);
                    if (compiledPattern != null && compiledPattern.matcher(requestPath).matches()) {
                        log.info("✅ [Cache-Regex] COINCIDENCIA: '{}' -> '{}'", requestPath, patternStr);
                        return module;
                    }
                } else {
                    // 🎯 Usar PathPattern de Spring para isPattern = false
                    PathPattern pattern = pathPatternParser.parse(patternStr);
                    if (pattern.matches(PathContainer.parsePath(requestPath))) {
                        log.info("✅ [Cache-Spring] COINCIDENCIA: '{}' -> '{}'", requestPath, patternStr);
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
     * Procesa el enrutamiento basado en el módulo encontrado
     */
    protected Mono<Void> processRouting(ModulesUrl module, String requestPath, ServerHttpRequest request,
                                      ServerWebExchange exchange,
                                      GatewayFilterChain chain) {
        // ✅ Incrementar contador de forwards
        Integer currentForwardCount = exchange.getAttribute(FORWARD_COUNT_KEY);
        if (currentForwardCount == null) {
            currentForwardCount = 0;
        }
        final Integer forwardCount = currentForwardCount + 1;
        exchange.getAttributes().put(FORWARD_COUNT_KEY, forwardCount);
        
        log.info("🔢 Incrementando forward count: {}/{} para módulo: {}", 
                forwardCount, MAX_FORWARD_COUNT, module.getModuleName());

        String newPath = processPath(requestPath, module);
        log.info("🔧 Path procesado: '{}' -> '{}' (Forward #{}/{})", 
                requestPath, newPath, forwardCount, MAX_FORWARD_COUNT);

        // ✅ Validar que la nueva ruta sea diferente para evitar bucles
        if (requestPath.equals(newPath)) {
            log.error("🚫 BUCLE DETECTADO: Nueva ruta igual a la original: '{}'", newPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        // ✅ Validar que la nueva ruta sea válida
        if (newPath == null || newPath.trim().isEmpty()) {
            log.error("🚫 Ruta procesada inválida: '{}' para ruta original: '{}'", newPath, requestPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        // Marcar como forwardeada
        exchange.getAttributes().put("GATEWAY_FORWARDED_REQUEST", true);
        log.debug("🔄 Marcando petición como forwardeada");

        URI originalUri = request.getURI();
        String queryString = originalUri.getRawQuery();

        // Construir la URI de redirección
        String forwardUriString = "forward:" + newPath;
        if (queryString != null && !queryString.isEmpty() && !newPath.contains("?")) {
            forwardUriString += "?" + queryString;
        }

        URI forwardUri;
        try {
            forwardUri = new URI(forwardUriString);
            log.info("🎯 Redirigiendo internamente: '{}' -> '{}' (Forward #{}/{})", 
                    requestPath, forwardUri, forwardCount, MAX_FORWARD_COUNT);
        } catch (URISyntaxException e) {
            log.error("🚫 Error al construir URI de redirección: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, forwardUri);

        // Preservar el cuerpo de la solicitud
        Mono<byte[]> cachedBody = DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);
                    return content;
                })
                .cache();

        // Crear la solicitud modificada
        ServerHttpRequest modifiedRequest;
        if (newPath.contains("?")) {
            modifiedRequest = request.mutate()
                    .path(newPath.substring(0, newPath.indexOf("?")))
                    .build();
        } else {
            modifiedRequest = request.mutate()
                    .path(newPath)
                    .build();
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // ✅ Transferir TODOS los atributos del exchange original
        for (String key : exchange.getAttributes().keySet()) {
            mutatedExchange.getAttributes().put(key, exchange.getAttributes().get(key));
        }

        // Continuar con la cadena de filtros
        return cachedBody
                .defaultIfEmpty(new byte[0])
                .flatMap(bytes -> {
                    if (bytes.length > 0) {
                        ServerHttpRequest requestWithBody = new ServerHttpRequestDecorator(modifiedRequest) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                DataBufferFactory bufferFactory = mutatedExchange.getResponse().bufferFactory();
                                DataBuffer buffer = bufferFactory.wrap(bytes);
                                return Flux.just(buffer);
                            }
                        };
                        return chain.filter(mutatedExchange.mutate().request(requestWithBody).build())
                                .doOnSuccess(v -> log.info("✅ Forward #{} completado: '{}' -> '{}'", forwardCount, requestPath, newPath))
                                .doOnError(e -> log.error("❌ Error en forward #{}: '{}' -> '{}': {}", forwardCount, requestPath, newPath, e.getMessage()));
                    } else {
                        return chain.filter(mutatedExchange)
                                .doOnSuccess(v -> log.info("✅ Forward #{} completado (sin cuerpo): '{}' -> '{}'", forwardCount, requestPath, newPath))
                                .doOnError(e -> log.error("❌ Error en forward #{}: '{}' -> '{}': {}", forwardCount, requestPath, newPath, e.getMessage()));
                    }
                });
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

                    for (ModulesUrl module : modules) {
                        if (module.getPath() != null && !module.getPath().isEmpty()) {
                            if (module.getIsPattern() != null && module.getIsPattern()) {
                                // Es un patrón regex
                                patternPathCache.put(module.getPath(), module);
                                // Pre-compilar regex para performance
                                getCompiledRegex(module.getPath());
                                log.debug("📋 Cacheado patrón REGEX: '{}'", module.getPath());
                            } else if (module.getPath().contains("*")) {
                                // Es un patrón de Spring
                                patternPathCache.put(module.getPath(), module);
                                log.debug("📋 Cacheado patrón SPRING: '{}'", module.getPath());
                            } else {
                                // Es una ruta exacta
                                exactPathCache.put(module.getPath(), module);
                                log.debug("📋 Cacheada ruta EXACTA: '{}'", module.getPath());
                            }
                        }
                    }

                    lastCacheRefresh.set(System.currentTimeMillis());
                    log.info("✅ Caché refrescado: {} rutas exactas, {} patrones, {} regex compilados",
                            exactPathCache.size(), patternPathCache.size(), compiledRegexCache.size());
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
                log.debug("📝 Regex compilado: '{}'", r);
                return pattern;
            } catch (Exception e) {
                log.error("🚫 Error compilando regex '{}': {}", r, e.getMessage());
                return null;
            }
        });
    }    

    public static class Config {
        // Puedes agregar configuración si es necesario
    }
}
