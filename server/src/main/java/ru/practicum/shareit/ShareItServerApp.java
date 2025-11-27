package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс микросервиса shareit-server.
 * Содержит всю бизнес-логику: работу с пользователями, вещами, бронированиями, запросами и комментариями.
 */
@SpringBootApplication
public class ShareItServerApp {
    public static void main(String[] args) {
        SpringApplication.run(ShareItServerApp.class, args);
    }
}