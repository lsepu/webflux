package com.example.ProjectReactorTwo;

import com.example.ProjectReactorTwo.models.Comentarios;
import com.example.ProjectReactorTwo.models.Usuario;
import com.example.ProjectReactorTwo.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class ProjectReactorTwoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(ProjectReactorTwoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProjectReactorTwoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ejemploIterable();
		ejemploFlatMap();
	}

	private void ejemploIterable() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres");
		usuariosList.add("Luis");
		usuariosList.add("Pedro");


		Flux<String> nombres = Flux.fromIterable(usuariosList)/*Flux.just("Andres", "Luis", "Pedro")*/
				.doOnNext(e -> {
					if(e.isEmpty()) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(e);
				}).map(String::toUpperCase);

		nombres.subscribe(element -> log.info(element), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				log.info("Ha finalizado la ejecución del observable con éxito!");
			}
		});
	}

	private void ejemploFlatMap() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres");
		usuariosList.add("Luis");
		usuariosList.add("Pedro");
		usuariosList.add("Bruce");
		usuariosList.add("Bruce");


		Flux.fromIterable(usuariosList)
				.flatMap(usuario -> {
					if(usuario.equalsIgnoreCase("bruce")){
						return Mono.just(usuario);
					}
					return Mono.empty();
				})
				.map(String::toUpperCase)
				.subscribe(element -> log.info(element));
	}

	private void ejemploCollectList() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres");
		usuariosList.add("Luis");
		usuariosList.add("Pedro");
		usuariosList.add("Bruce");
		usuariosList.add("Bruce");


		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
				});
	}

	public void ejemploUsuarioComentariosFlatMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> {
			return new Usuario("John", "Doe");
		});

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Nuevo comentario!");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> {
			return new Usuario("John", "Doe");
		});

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Nuevo comentario!");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios =
				usuarioMono.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentarios(usuario, comentarios));

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));

	}

	public void ejemploUsuarioComentariosZipWithForma2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> {
			return new Usuario("John", "Doe");
		});

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Nuevo comentario!");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios =
				usuarioMono.zipWith(comentariosMono)
								.map(tuple -> {
									Usuario u = tuple.getT1();
									Comentarios c = tuple.getT2();
									return new UsuarioComentarios(u,c);
								});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));

	}

	public void ejemploZipWithRangos(){
		Flux.just(1, 2, 3, 4)
				.map( i -> i * 2)
				.zipWith(Flux.range(0, 4), (one, two) -> String.format("First Flux %d, Second Flux %d", one, two))
				.subscribe(text -> log.info(text));

	}

	public void ejemploInterval(){
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				.subscribe();
	}

	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1,12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.subscribe();
	}

	public void ejemploIntervalInfinito() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if( i>= 5) {
						return Flux.error(new InterruptedException("Until 5"));
					}
					return Flux.just(i);
				})
				.map( i -> "Hola "+i)
				.retry(2)
				.subscribe(log::info, e -> log.error(e.getMessage()));

		latch.wait();
	}

	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if(contador == 10){
						timer.cancel();
						emitter.complete();
					}
				}
			}, 1000, 1000);
		})
				.subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()), () -> log.info("Hemos terminado"));
	}

	public void ejemploContraPresion() {
		Flux.range(1, 10)
				.log()
				.limitRate(5)
				.subscribe();
	}




}
