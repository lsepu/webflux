package com.lsepu.spring_boot_webflux_client.handler;

import com.lsepu.spring_boot_webflux_client.models.Producto;
import com.lsepu.spring_boot_webflux_client.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService service;

    public Mono<ServerResponse> listar(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request){
        String id = request.pathVariable("id");
        return errorHandler(
                service.findById(id)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .syncBody(p))
                .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> crear(ServerRequest request){
        Mono<Producto> producto = request.bodyToMono(Producto.class);

        return errorHandler(
                producto.flatMap(p -> {
                    if(p.getCreateAt() == null){
                        p.setCreateAt(new Date());
                    }
                    return service.save(p);
                    }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .syncBody(p))
                );
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return errorHandler(
                producto
                .flatMap(p -> service.update(p, id))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(p))
        );
    }


    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");

        return service.delete(id).then(ServerResponse.noContent().build())
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if(errorResponse.getStatusCode() == HttpStatus.NOT_FOUND){
                        return ServerResponse.notFound().build();
                    }
                    return Mono.error(errorResponse);
                });
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response){
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if(errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST){
                return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .syncBody(errorResponse.getResponseBodyAsString());
            }
            return Mono.error(errorResponse);
        });
    }



}
