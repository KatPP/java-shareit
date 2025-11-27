package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс микросервиса shareit-gateway.
 * Принимает HTTP-запросы от клиентов и перенаправляет их в shareit-server.
 */
@SpringBootApplication
public class ShareItGatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(ShareItGatewayApp.class, args);
    }
}