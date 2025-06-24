package com.walrex.gateway.gateway.infrastructure.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtHeaderFilter(ReactiveJwtDecoder jwtDecoder) {
        super(Config.class);
        this.jwtDecoder=jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
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

            String token = extractBearerToken(exchange.getRequest());
            if (token == null) {
                log.warn("Token JWT no encontrado para ruta: {}", path);
                return unauthorizedResponse(exchange, "Token requerido");
            }

            return jwtDecoder.decode(token)
                    .cast(Jwt.class)
                    .flatMap(jwt->{
                        try{
                            // Extraer datos del claim 'data'
                            Map<String, Object> data = jwt.getClaimAsMap("data");
                            if (data != null) {
                                log.info("🎫 === CAMPOS INDIVIDUALES ===");
                                data.forEach((key, value) -> {
                                    log.info("🎫 Key: '{}', Value: '{}', Class: {}",
                                            key, value, value != null ? value.getClass().getSimpleName() : "NULL");
                                });
                            }

                            String userId = getClaimAsString(data, "id");
                            String username = (String) data.get("username");
                            String idRol = getClaimAsString(data, "idrol");
                            String empleado = (String) data.get("apenom_employee");
                            @SuppressWarnings("unchecked")
                            String role = (String) data.get("role");

                            List<String> permissions = null;
                            Object permissionsObj = data.get("permission");
                            if(permissionsObj instanceof List){
                                @SuppressWarnings("unchecked")
                                List<String> permissionsList = (List<String>) permissionsObj;
                                permissions=permissionsList;
                            }
                            // Validar campos requeridos
                            if (userId == null || username == null) {
                                log.error("Claims requeridos faltantes en JWT");
                                return unauthorizedResponse(exchange, "Token inválido");
                            }

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username)
                                    .header("X-User-Role-Id", idRol != null ? idRol : "")
                                    .header("X-User-Role", role != null ? role : "")
                                    .header("X-Employee-Name", empleado != null ? empleado : "")
                                    .header("X-User-Permissions", permissions != null ? String.join(",", permissions) : "")
                                    .header("X-Token-Audience", jwt.getAudience() != null ? String.join(",", jwt.getAudience()) : "")
                                    .header("Authorization", "Bearer " + token)
                                    .build();

                            log.debug("Usuario autenticado: {} ({}) - Redirigiendo a: {}", username, userId, path);
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        }catch(Exception e){
                            log.error("Error procesando claims del JWT: {}", e.getMessage());
                            return unauthorizedResponse(exchange, "Error procesando token");
                        }
                    })
                    .onErrorResume(JwtException.class, ex -> {
                        log.error("JWT inválido: {}", ex.getMessage());
                        return unauthorizedResponse(exchange, "Token inválido");
                    })
                    .onErrorResume(throwable ->
                                    throwable instanceof JwtException ||
                                            throwable instanceof SecurityException ||
                                            throwable.getCause() instanceof JwtException,
                            ex -> {
                                log.error("Error de autenticación: {}", ex.getMessage());
                                return unauthorizedResponse(exchange, "Error de autenticación");
                            }
                    );
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
        private List<String> publicPaths = List.of("api/v2/auth", "auth", "document", "graphiql", "graphql");
        private boolean enabled = true;

        public List<String> getPublicPaths() { return publicPaths; }
        public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
