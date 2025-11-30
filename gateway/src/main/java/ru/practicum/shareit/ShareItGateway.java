package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение ShareIt Gateway.
 * Отвечает за валидацию входящих запросов и проксирование их в ShareIt Server.
 */
@SpringBootApplication
public class ShareItGateway {
	public static void main(String[] args) {
		SpringApplication.run(ShareItGateway.class, args);
	}
}