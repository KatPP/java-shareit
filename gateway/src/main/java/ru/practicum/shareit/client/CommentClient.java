package ru.practicum.shareit.client;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * Клиент для комментариев (реально вызывается через ItemClient).
 */
@Service
@RequiredArgsConstructor
public class CommentClient {
    private final RestTemplateBuilder builder;
    @Value("${shareit.server.url}")
    private String serverUrl;

    public ResponseEntity<Object> addComment(Long userId, Long itemId, Object commentDto) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        org.springframework.http.HttpEntity<Object> entity = new org.springframework.http.HttpEntity<>(commentDto, headers);
        return getRestTemplate().exchange("/items/" + itemId + "/comment", org.springframework.http.HttpMethod.POST, entity, Object.class);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        var httpClient = HttpClients.createDefault();
        var requestFactory = new HttpComponentsClientHttpRequestFactory((HttpClient) httpClient);
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> requestFactory)
                .build();
    }
}