package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseClient {
    @Value("${shareit.server.url}")
    protected String serverUrl;

    protected final RestTemplateBuilder builder;

    protected BaseClient(RestTemplateBuilder builder) {
        this.builder = builder;
    }

    protected HttpEntity<Object> createEntityWithHeader(Object body, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        return new HttpEntity<>(body, headers);
    }

    protected HttpEntity<Object> mapToHttpEntity(Object body) {
        return new HttpEntity<>(body);
    }
}