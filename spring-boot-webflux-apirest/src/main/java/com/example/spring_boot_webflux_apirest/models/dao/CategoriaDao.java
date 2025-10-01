package com.example.spring_boot_webflux_apirest.models.dao;

import com.example.spring_boot_webflux_apirest.models.documents.Categoria;
import com.example.spring_boot_webflux_apirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

    public Mono<Categoria> findByNombre(String nombre);
}
