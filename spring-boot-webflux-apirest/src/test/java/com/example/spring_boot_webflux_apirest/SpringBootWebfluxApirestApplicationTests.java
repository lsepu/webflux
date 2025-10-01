package com.example.spring_boot_webflux_apirest;

import com.example.spring_boot_webflux_apirest.models.documents.Categoria;
import com.example.spring_boot_webflux_apirest.models.documents.Producto;
import com.example.spring_boot_webflux_apirest.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

//@AutoConfigureWebTestClient
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService service;

	@Test
	public void listarTest() {

		client.get()
				.uri("/api/v2/productos")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					productos.forEach(p -> {
						System.out.println(p.getNombre());
					});

					Assertions.assertEquals(productos.size(),9);

				} );
	}

	@Test
	public void verTest() {
		Mono<Producto> producto = service.findByNombre("TV Panasonic Pantalla LCD");
		client.get()
				.uri("/api/v2/productos/{id}", Collections.singletonMap("id",producto.block().getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");
	}

	@Test
	public void crearTest() {

		Categoria categoria = service.findCategoriaByNombre("Muebles").block();

		Producto producto = new Producto();
		producto.setNombre("Mesa comedor");
		producto.setPrecio(100.00);
		producto.setCategoria(categoria);

		client.post().uri("/api/v2/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	public void editarTest(){

		Producto producto = service.findByNombre("Sony Notebook").block();
		Categoria categoria = service.findCategoriaByNombre("Electrónico").block();

		Producto productoEditado = new Producto();
		productoEditado.setNombre("Asus Notebook");
		productoEditado.setPrecio(200.00);
		productoEditado.setCategoria(categoria);

		client.put()
				.uri("/api/v2/productos/{id}", Collections.singletonMap("id",producto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Asus Notebook")
				.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}


	@Test
	public void EliminarTest(){
		Producto producto = service.findByNombre("Mica Cómoda 5 Cajones").block();
		client.delete()
				.uri("/api/v2/productos/{id}", Collections.singletonMap("id",producto.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();

		client.get()
				.uri("/api/v2/productos/{id}", Collections.singletonMap("id",producto.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.isEmpty();

	}
}
