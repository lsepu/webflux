package com.lsepu.spring_boot_webflux_client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient registrarWebClient(){
        return WebClient.create("http://localhost:8080/api/v2/productos");
    }

}
