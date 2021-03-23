package com.lny.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDiscoveryApplication.class, args);
    }

//    @Bean
//    RedisRateLimiter redisRateLimiter() {
//        return new RedisRateLimiter(5, 10);
//    }

    @Bean
    SecurityWebFilterChain authorization(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .httpBasic(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ae -> ae
                        .pathMatchers("/hello").authenticated()
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    MapReactiveUserDetailsService authentication() {
        return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
                .username("leo")
                .password("123")
                .roles("USER")
                .build());
    }

    @Bean
    ApplicationListener<RefreshRoutesResultEvent> routesRefreshed() {
        return refreshRoutesResultEvent -> {
            System.out.println("Routes updated");
            var cachingRouteLocator = (CachingRouteLocator) refreshRoutesResultEvent.getSource();
            Flux<Route> routes = cachingRouteLocator.getRoutes();
            routes.subscribe(System.out::println);
        };
    }

    //circuit-breaker
    @Bean
    RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
                .routes()

                .route(routeSpec -> routeSpec
                        .path("/hello")
//                        .filters(filterSpec -> filterSpec
//                                        .requestRateLimiter(requestRateLimit -> requestRateLimit
//                                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(new PrincipalNameKeyResolver())
//                                        )
//                        )
                        .uri("lb://product-service"))

                .route(routeSpec -> routeSpec
                        .path("/default")
                        .filters(filterSpec -> filterSpec
                                .filter((exchange, chain) -> {
                                    System.out.println("This is your second chance!");
                                    System.out.println("URL: " + exchange.getRequest().getURI());
                                    return chain.filter(exchange);
                                }))
                        .uri("https://spring.io/guides")
                )
                .route(routeSpec -> routeSpec
                        .path("/products")
                        .filters(filterSpec -> filterSpec
                                .circuitBreaker(
                                        config -> config.setFallbackUri("forward:/default")
                                ))
                        .uri("lb://product-service")
                )
                .route(routeSpec -> routeSpec
                        .path("/error/**")
                        .filters(filterSpec -> filterSpec.retry(5))
                        .uri("lb://product-service")
                )
                .build();
    }

}
