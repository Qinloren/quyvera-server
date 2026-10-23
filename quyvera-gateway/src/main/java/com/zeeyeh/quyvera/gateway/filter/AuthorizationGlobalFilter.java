package com.zeeyeh.quyvera.gateway.filter;

import com.zeeyeh.quyvera.gateway.config.properties.GatewaySecurityProperties;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Qinloren
 * @date 2026/10/20 19:50
 */
@NullMarked
@Component
public class AuthorizationGlobalFilter implements GlobalFilter, Ordered {

    private final GatewaySecurityProperties properties;

    public AuthorizationGlobalFilter(GatewaySecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (properties.getWhitelist().stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        return exchange.getPrincipal()
                .cast(Jwt.class)
                .flatMap(jwt -> {
                    List<String> roles = jwt.getClaimAsStringList("roles");
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-User-Id", jwt.getSubject())
                            .header("X-User-Name", jwt.getClaimAsString("username"))
                            .header("X-User-Roles", String.join(",", roles
                                     == null ? List.of() : roles))
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }))
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
