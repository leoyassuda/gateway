package com.lny.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDiscoveryApplication.class, args);
    }

//    @Bean
//    RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder) {
//        return routeLocatorBuilder
//                .routes()
//                .route(routeSpec ->
//                        routeSpec
//                                .path("/hello")
//                                .filters(gatewayFilterSpec ->
//                                        gatewayFilterSpec.setPath("/guides")
//                                )
//                                .uri("https://spring.io")
//                )
//                .route("twitter", routeSpec ->
//                        routeSpec.path("/twitter/**")
//                        .filters(gatewayFilterSpec ->
//                                gatewayFilterSpec.rewritePath(
//                                        "/twitter/(?<handle>.*)",
//                                        "/${handle}")
//                        )
//                        .uri("http://twitter.com/@")
//                )
//                .build();
//    }
}
