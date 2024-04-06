package com.example.demo.cert.client.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.cert.client.SslConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/client")
public class ClientController {

    /**
     * This property bean is created in {@link SslConfig#webClient()}
     **/
    @Autowired
    WebClient webClient;

    /**
     * This controller method invokes the server using the wired bean
     * {@link this#webClient} and returns back the secured data from the server
     *
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @GetMapping
    public String gatherDataFromServer() {
        log.warn("This is client logger from application.");
        Mono<String> dateFromServer = webClient.get().uri("https://localhost:8082/server").retrieve()
                .bodyToMono(String.class);
        return dateFromServer.block();
    }
}