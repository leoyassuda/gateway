package com.lny.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDiscoveryApplication.class, args);
    }

    @Bean
    ApplicationListener<RefreshRoutesResultEvent> routesRefreshed() {
        return new ApplicationListener<RefreshRoutesResultEvent>() {
            @Override
            public void onApplicationEvent(RefreshRoutesResultEvent refreshRoutesResultEvent) {
                System.out.println("Routes updated");
                var cachingRouteLocator = (CachingRouteLocator) refreshRoutesResultEvent.getSource();
                Flux<Route> routes = cachingRouteLocator.getRoutes();
                routes.subscribe(System.out::println);
            }
        };
    }

    @Bean
    RouteLocator gateway(SetPathGatewayFilterFactory filterFactory) {
        var singleRoute = Route.async()
                .id("test-route")
                .filter(new OrderedGatewayFilter(
                        filterFactory.apply(config ->
                                config.setTemplate("/products")), 1
                ))
                .uri("lb://product-service")
                .asyncPredicate(serverWebExchange -> {
                    var uri = serverWebExchange.getRequest().getURI();
                    var path = uri.getPath();
                    var match = path.contains("/products");
                    return Mono.just(match);
                })
                .build();

        return () -> Flux.just(singleRoute);

    }
}
