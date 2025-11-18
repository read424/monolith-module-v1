package com.walrex.gateway.gateway.infrastructure.config.filter;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtHeaderFilter(ReactiveJwtDecoder jwtDecoder) {
        super(Config.class);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            Boolean jwtProcessed = exchange.getAttribute("JWT_ALREADY_PROCESSED");
            if (jwtProcessed != null && jwtProcessed) {
                log.debug("✅ JWT ya procesado anteriormente");
                return chain.filter(exchange);
            }

            // ✅ LOG INICIAL - Confirmar que el filtro se ejecuta
            log.error("[JWT-FILTER-START] JwtHeaderFilter INICIANDO para: {} {} - Thread: {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath().value(),
                    Thread.currentThread().getName());

            log.error("[GATEWAY-JWT] Procesando: {} {} - Thread: {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath().value(),
                    Thread.currentThread().getName());

            String path = exchange.getRequest().getPath().value();
            String fullUri = exchange.getRequest().getURI().toString();
            String originalPath = exchange.getAttribute("ORIGINAL_PATH");
            String threadName = Thread.currentThread().getName();

            // ✅ Usar la ruta original para verificación
            String pathToCheck = originalPath != null ? originalPath : path;

            log.error("🔴 [4] JwtHeaderFilter [{}] - Path actual: '{}', Original: '{}', Verificando: '{}'",
                    threadName, path, originalPath, pathToCheck);

            // ✅ Verificar si es una petición forward
            Boolean isForwarded = exchange.getAttribute("GATEWAY_FORWARDED_REQUEST");
            if (isForwarded != null && isForwarded) {
                log.error("🔴 [4] JwtHeaderFilter - SALTANDO (ya forwardeada)");
                return chain.filter(exchange);
            }

            // Verificar rutas públicas
            if (isPublicPath(pathToCheck, config.getPublicPaths())) {
                log.error("✅ [4] JwtHeaderFilter - Ruta pública detectada: '{}'", pathToCheck);
                return chain.filter(exchange);
            }

            // ✅ LOG DETALLADO: Verificar headers de autorización
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.error("🔍 [4] JwtHeaderFilter - Authorization Header: '{}'", authHeader);

            String token = extractBearerToken(exchange.getRequest());
            log.error("🔍 [4] JwtHeaderFilter - Token extraído: {}",
                    token != null ? "PRESENTE (" + token.substring(0, Math.min(20, token.length())) + "...)" : "NULL");

            if (token == null) {
                log.error("❌ [4] JwtHeaderFilter - Token JWT no encontrado para ruta: {}", path);
                return unauthorizedResponse(exchange, "Token requerido");
            }

            log.error("🔍 [4] JwtHeaderFilter - Iniciando decodificación JWT...");
            return jwtDecoder.decode(token)
                    .cast(Jwt.class)
                    .doOnNext(jwt -> log.error(
                            "✅ [4] JwtHeaderFilter - JWT decodificado exitosamente. Subject: '{}', Issuer: '{}'",
                            jwt.getSubject(), jwt.getIssuer()))
                    .flatMap(jwt -> {
                        try {
                            log.error("🔍 [4] JwtHeaderFilter - Procesando claims del JWT...");
                            // Extraer datos del claim 'data'
                            Map<String, Object> data = jwt.getClaimAsMap("data");
                            log.error("🔍 [4] JwtHeaderFilter - Claim 'data' presente: {}", data != null);

                            if (data != null) {
                                log.error("🎫 === CAMPOS INDIVIDUALES ===");
                                data.forEach((key, value) -> {
                                    log.error("🎫 Key: '{}', Value: '{}', Class: {}",
                                            key, value, value != null ? value.getClass().getSimpleName() : "NULL");
                                });
                            } else {
                                log.error("❌ [4] JwtHeaderFilter - Claim 'data' es NULL en el JWT");
                            }

                            String userId = getClaimAsString(data, "id");
                            String username = (String) data.get("username");
                            String idRol = getClaimAsString(data, "idrol");
                            String empleado = (String) data.get("apenom_employee");
                            @SuppressWarnings("unchecked")
                            String role = (String) data.get("role");

                            log.error(
                                    "🔍 [4] JwtHeaderFilter - Claims extraídos - userId: '{}', username: '{}', role: '{}'",
                                    userId, username, role);

                            List<String> permissions = null;
                            Object permissionsObj = data.get("permission");
                            if (permissionsObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> permissionsList = (List<String>) permissionsObj;
                                permissions = permissionsList;
                                log.error("🔍 [4] JwtHeaderFilter - Permisos encontrados: {}", permissions);
                            } else {
                                log.error("🔍 [4] JwtHeaderFilter - Permisos no encontrados o formato incorrecto: {}",
                                        permissionsObj);
                            }

                            // Validar campos requeridos
                            if (userId == null || username == null) {
                                log.error(
                                        "❌ [4] JwtHeaderFilter - Claims requeridos faltantes - userId: '{}', username: '{}'",
                                        userId, username);
                                return unauthorizedResponse(exchange, "Token inválido");
                            }

                            log.error("✅ [4] JwtHeaderFilter - Claims válidos, agregando headers...");
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username)
                                    .header("X-User-Role-Id", idRol != null ? idRol : "")
                                    .header("X-User-Role", role != null ? role : "")
                                    .header("X-Employee-Name", empleado != null ? empleado : "")
                                    .header("X-User-Permissions",
                                            permissions != null ? String.join(",", permissions) : "")
                                    .header("X-Token-Audience",
                                            jwt.getAudience() != null ? String.join(",", jwt.getAudience()) : "")
                                    .header("Authorization", "Bearer " + token)
                                    .build();

                            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                            // ✅ Transferir atributos críticos que no deben perderse
                            mutatedExchange.getAttributes().putAll(exchange.getAttributes());

                            // ✅ Marcar que JWT ya fue procesado
                            mutatedExchange.getAttributes().put("JWT_ALREADY_PROCESSED", true);

                            log.error(
                                "✅ [4] JwtHeaderFilter - Headers agregados, continuando con la cadena de filtros");
                            return chain.filter(mutatedExchange);
                        } catch (Exception e) {
                            log.error("❌ [4] JwtHeaderFilter - Error procesando claims del JWT: {}", e.getMessage(), e);
                            return unauthorizedResponse(exchange, "Error procesando token");
                        }
                    })
                    .onErrorResume(JwtException.class, ex -> {
                        log.error("❌ [4] JwtHeaderFilter - JWT inválido: {}", ex.getMessage(), ex);
                        return unauthorizedResponse(exchange, "Token inválido");
                    })
                    .onErrorResume(throwable -> {
                        log.error("❌ [4] JwtHeaderFilter - Error general de autenticación: {}", throwable.getMessage(),
                                throwable);
                        return unauthorizedResponse(exchange, "Error de autenticación");
                    });
        });
    }

    private String extractBearerToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path, List<String> publicPaths) {
        log.info("🔍 Verificando ruta pública - Path: '{}', PublicPaths: {}", path, publicPaths);

        boolean isPublic = publicPaths.stream().anyMatch(publicPath -> {
            boolean matches = path.startsWith("/" + publicPath + "/") ||
                    path.equals("/" + publicPath) ||
                    path.startsWith("/" + publicPath);
            // ✅ Log de cada verificación
            log.debug("   Verificando '{}' contra '{}': {}", path, publicPath, matches);
            return matches;
        });
        log.info("🎯 Resultado final para '{}': {}", path, isPublic ? "PÚBLICA" : "REQUIERE TOKEN");
        return isPublic;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    private String getClaimAsString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    public static class Config {
        private List<String> publicPaths = List.of(
                "api/v2/auth",
                "auth",
                "document",
                "swagger-ui",
                "api-docs",
                "graphql",
                "graphiql",
                "health",
                "actuator",
                "metrics",
                "prometheus");
        private boolean enabled = true;

        public List<String> getPublicPaths() {
            return publicPaths;
        }

        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
