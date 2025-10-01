package com.example.spring_boot_webflux_apirest;

import com.example.spring_boot_webflux_apirest.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;

import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){
        return route(GET("/api/v2/productos"), handler::listar)
                .andRoute(GET("/api/v2/productos/{id}").and(contentType(MediaType.APPLICATION_JSON)), handler::ver)
                .andRoute(POST("/api/v2/productos/"),handler::crear)
                .andRoute(PUT("/api/v2/productos/{id}"), handler::editar);
    }

}
