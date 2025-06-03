package com.walrex.gateway.gateway.config;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class DynamicModuleRouteFilter extends AbstractGatewayFilterFactory<DynamicModuleRouteFilter.Config> {
    private final ModulesUrlRepository modulesUrlRepository;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    // Caché para mejorar el rendimiento
    private final ConcurrentHashMap<String, ModulesUrl> exactPathCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ModulesUrl> patternPathCache = new ConcurrentHashMap<>();
    private final AtomicLong lastCacheRefresh = new AtomicLong(System.currentTimeMillis());
    private final long CACHE_TTL = 60000; // 1 minuto

    public DynamicModuleRouteFilter(ModulesUrlRepository modulesUrlRepository) {
        super(Config.class);
        this.modulesUrlRepository = modulesUrlRepository;
    }

    @Override
    public GatewayFilter apply(Config config){
        return ((exchange, chain) -> {
            // ✅ Verificar si es una petición forward para evitar bucles
            String path = exchange.getRequest().getPath().value();
            String threadName = Thread.currentThread().getName();
            log.error("🟣 [5] DynamicModuleRouteFilter [{}] - Path: '{}'", threadName, path);

            Boolean isForwarded = exchange.getAttribute("GATEWAY_FORWARDED_REQUEST");
            if (isForwarded != null && isForwarded) {
                log.debug("Petición ya forwardeada, saltando DynamicModuleRouteFilter");
                return chain.filter(exchange);
            }

            // Obtener o inicializar el contador de redirecciones
            Integer redirectCount = exchange.getAttribute("REDIRECT_COUNT");
            if (redirectCount == null) {
                redirectCount = 0;
            }
            // Incrementar el contador
            redirectCount++;
            exchange.getAttributes().put("REDIRECT_COUNT", redirectCount);

            // Verificar si se ha excedido el límite de redirecciones
            if (redirectCount > 3) {
                log.error("⚠️ Detectado posible bucle de redirección, abortando después de {} intentos", redirectCount);
                exchange.getResponse().setStatusCode(HttpStatus.LOOP_DETECTED);
                return exchange.getResponse().setComplete();
            }

            // Verificar si esta solicitud ya ha sido procesada por este filtro
            Boolean processed = exchange.getAttribute("DYNAMIC_MODULE_ROUTE_PROCESSED");
            if (processed != null && processed) {
                // Esta solicitud ya ha sido procesada, pasar al siguiente filtro
                return chain.filter(exchange);
            }
            // Marcar esta solicitud como procesada para evitar bucles
            exchange.getAttributes().put("DYNAMIC_MODULE_ROUTE_PROCESSED", true);

            ServerHttpRequest request = exchange.getRequest();
            // Intentar obtener la ruta original si está disponible
            String requestPath;
            if (exchange.getAttribute("ORIGINAL_PATH") != null) {
                requestPath = exchange.getAttribute("ORIGINAL_PATH");
                log.info("Procesando ruta original desde atributo: {}", requestPath);
            } else {
                requestPath = request.getPath().value();
                log.info("Procesando ruta: {}", requestPath);
                // Si la ruta es solo "/", podría indicar que perdimos la ruta original
                if ("/".equals(requestPath)) {
                    log.warn("⚠️ Se recibió una ruta vacía (/), lo cual podría indicar un problema de redirección");
                }
            }

            if(System.currentTimeMillis()-lastCacheRefresh.get()>CACHE_TTL){
                refreshCache();
            }
            AtomicBoolean foundModule = new AtomicBoolean(false);

            // Buscar primero en el caché de rutas exactas
            ModulesUrl cachedExactModule = exactPathCache.get(requestPath);
            if (cachedExactModule != null) {
                log.info("Módulo encontrado en caché (coincidencia exacta): {}", requestPath);
                return processModule(cachedExactModule, requestPath, request, exchange, chain, foundModule);
            }

            // Buscar en el caché de patrones
            ModulesUrl cachedPatternModule = findPatternMatch(requestPath);
            if (cachedPatternModule != null) {
                log.info("Módulo encontrado en caché (coincidencia de patrón): {}", requestPath);
                return processModule(cachedPatternModule, requestPath, request, exchange, chain, foundModule);
            }

            // Si no está en caché, buscar en la base de datos
            return modulesUrlRepository.findByPath(requestPath)
                .doOnNext(module -> {
                    log.info("Módulo encontrado en BD (coincidencia exacta) para ruta: {}", requestPath);
                    foundModule.set(true);
                    exactPathCache.put(requestPath, module);
                })
                .switchIfEmpty(findModuleByPattern(requestPath)
                        .doOnNext(module -> {
                            log.info("Módulo encontrado en BD (coincidencia de patrón) para ruta: {}", requestPath);
                            foundModule.set(true);
                            // No guardar en caché de rutas exactas, pero sí usarlo para el procesamiento
                }))
                .flatMap(module -> processRouting(module, requestPath, request, exchange, chain))
                .then(Mono.defer(() -> {
                    if (!foundModule.get()) {
                        log.warn("No se encontró configuración para la ruta: {}", requestPath);
                        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                        return exchange.getResponse().setComplete();
                    }
                    return Mono.empty();
                }));
        });
    }

    /**
     * Procesa un módulo encontrado y aplica el enrutamiento correspondiente
     */
    private Mono<Void> processModule(ModulesUrl module, String requestPath, ServerHttpRequest request,
                                     ServerWebExchange exchange,
                                     GatewayFilterChain chain,
                                     AtomicBoolean foundModule) {
        foundModule.set(true);
        return processRouting(module, requestPath, request, exchange, chain);
    }

    /**
     * Busca en el caché de patrones un módulo que coincida con la ruta solicitada
     */
    private ModulesUrl findPatternMatch(String requestPath) {
        for (String patternStr : patternPathCache.keySet()) {
            try {
                PathPattern pattern = pathPatternParser.parse(patternStr);
                if (pattern.matches(PathContainer.parsePath(requestPath))) {
                    return patternPathCache.get(patternStr);
                }
            } catch (Exception e) {
                // Ignorar errores de patrón y continuar con el siguiente
            }
        }
        return null;
    }

    /**
     * Procesa el enrutamiento basado en el módulo encontrado
     */
    private Mono<Void> processRouting(ModulesUrl module, String requestPath, ServerHttpRequest request,
                                      ServerWebExchange exchange,
                                      GatewayFilterChain chain) {
        String newPath = processPath(requestPath, module);
        log.info("Path procesado para redirección interna: {}", newPath);

        // Almacenar la URI original para depuración
        URI originalUri = request.getURI();
        log.debug("URI original: {}", originalUri);

        // Obtener los parámetros de consulta originales
        String queryString = originalUri.getRawQuery();

        // Construir la URI de redirección con los parámetros de consulta
        String forwardUriString = "forward:" + newPath;
        if (queryString != null && !queryString.isEmpty()  && !newPath.contains("?")) {
            forwardUriString += "?" + queryString;
        }

        // Para módulos en la misma aplicación, usamos forward:/ como esquema
        URI forwardUri;
        try {
            forwardUri = new URI(forwardUriString);
            log.info("- Redirigiendo internamente a: {}", forwardUri);
        } catch (URISyntaxException e) {
            log.error("Error al construir URI de redirección: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        // Configurar el atributo específico para redirecciones internas
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, forwardUri);

        // Asegurarnos de preservar el cuerpo de la solicitud
        Mono<byte[]> cachedBody = DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);
                    return content;
                })
                .cache();

        // Crear la solicitud modificada con la nueva ruta
        ServerHttpRequest modifiedRequest;
        if (newPath.contains("?")) {
            // Si newPath ya contiene parámetros de consulta, usarlo directamente
            modifiedRequest = request.mutate()
                    .path(newPath.substring(0, newPath.indexOf("?")))
                    .build();
        } else {
            // Si no, usar el path simple
            modifiedRequest = request.mutate()
                    .path(newPath)
                    .build();
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // Transferir los atributos importantes del exchange original al nuevo
        for (String key : exchange.getAttributes().keySet()) {
            mutatedExchange.getAttributes().put(key, exchange.getAttributes().get(key));
        }
        mutatedExchange.getAttributes().put("GATEWAY_FORWARDED_REQUEST", true);
        log.debug("🔄 Marcando petición como forwardeada para evitar bucles");

        log.debug("🔄 Request modificado - Headers: {}", modifiedRequest.getHeaders());
        log.debug("🔄 Request modificado - Method: {}", modifiedRequest.getMethod());
        log.debug("🔄 Request modificado - Path: {}", modifiedRequest.getPath());

        // Continuar con la cadena de filtros, inyectando el cuerpo preservado si es necesario
        return cachedBody
                .defaultIfEmpty(new byte[0])
                .flatMap(bytes -> {
                    if (bytes.length > 0) {
                        // Si hay cuerpo, lo preservamos
                        ServerHttpRequest requestWithBody = new ServerHttpRequestDecorator(modifiedRequest) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                DataBufferFactory bufferFactory = mutatedExchange.getResponse().bufferFactory();
                                DataBuffer buffer = bufferFactory.wrap(bytes);
                                return Flux.just(buffer);
                            }
                        };
                        return chain.filter(mutatedExchange.mutate().request(requestWithBody).build())
                                .doOnSuccess(v -> log.info("Procesamiento completado para ruta interna: {}", newPath))
                                .doOnError(e -> log.error("Error en procesamiento de ruta interna {}: {}", newPath, e.getMessage()));
                    } else {
                        // Si no hay cuerpo, continuamos con la solicitud modificada
                        return chain.filter(mutatedExchange)
                                .doOnSuccess(v -> log.info("Procesamiento completado para ruta interna (sin cuerpo): {}", newPath))
                                .doOnError(e -> log.error("Error en procesamiento de ruta interna {}: {}", newPath, e.getMessage()));
                    }
                });
    }

    /**
     * Encuentra un módulo que coincida con un patrón para la ruta dada
     */
    private Mono<ModulesUrl> findModuleByPattern(String requestPath) {
        return modulesUrlRepository.findAll()
                .filter(module -> {
                    if (module.getPath() == null || module.getPath().isEmpty()) {
                        return false;
                    }

                    // Intentar interpretar la ruta como un patrón
                    try {
                        PathPattern pattern = pathPatternParser.parse(module.getPath());
                        boolean matches = pattern.matches(PathContainer.parsePath(requestPath));
                        if (matches) {
                            // Si coincide, guardar en el caché de patrones
                            patternPathCache.put(module.getPath(), module);
                        }
                        return matches;
                    } catch (Exception e) {
                        log.warn("Error al evaluar patrón {} para ruta {}: {}",
                                module.getPath(), requestPath, e.getMessage());
                        return false;
                    }
                })
                .next();
    }

    /**
     * Extrae la parte de la ruta después del patrón configurado
     */
    private String processPath(String requestPath, ModulesUrl module) {
        // Si hay un stripPrefixCount, aplicarlo
        if (module.getStripPrefixCount() != null && module.getStripPrefixCount() > 0) {
            String[] segments = requestPath.split("/");
            StringBuilder strippedPath = new StringBuilder();
            int segmentsToSkip = module.getStripPrefixCount();

            // Construir la nueva ruta omitiendo los segmentos indicados
            for (int i = segmentsToSkip+1; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    strippedPath.append("/").append(segments[i]);
                }
            }

            return strippedPath.length() > 0 ? strippedPath.toString() : "/";
        }
        // Si no hay stripPrefix, usar la ruta completa
        return requestPath;
    }

    /**
     * Refresca el caché de rutas desde la base de datos
     */
    private void refreshCache() {
        log.info("Refrescando caché de rutas...");
        modulesUrlRepository.findAll()
                .collectList()
                .subscribe(modules -> {
                    exactPathCache.clear();
                    patternPathCache.clear();

                    for (ModulesUrl module : modules) {
                        if (module.getPath() != null && !module.getPath().isEmpty()) {
                            if (module.getPath().contains("*")) {
                                // Es un patrón
                                patternPathCache.put(module.getPath(), module);
                            } else {
                                // Es una ruta exacta
                                exactPathCache.put(module.getPath(), module);
                            }
                        }
                    }

                    lastCacheRefresh.set(System.currentTimeMillis());
                    log.info("Caché refrescado: {} rutas exactas, {} patrones",
                            exactPathCache.size(), patternPathCache.size());
                });
    }

    public static class Config {
        // Puedes agregar configuración si es necesario
    }
}
