package ru.practicum.shareit.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
@RequiredArgsConstructor
public class ItemClient {
    private final RestTemplateBuilder builder;
    @Value("${shareit.server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(Long userId, Object itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(itemDto, headers);
        return getRestTemplate().postForEntity("/items", request, Object.class);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, Object itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(itemDto, headers);
        return getRestTemplate().exchange(
                "/items/" + itemId,
                org.springframework.http.HttpMethod.PATCH,
                request,
                Object.class
        );
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/items/" + itemId, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> getOwnerItems(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/items", org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> search(String text) {
        return getRestTemplate().getForEntity("/items/search?text=" + text, Object.class);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, Object commentDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(commentDto, headers);
        return getRestTemplate().postForEntity("/items/" + itemId + "/comment", request, Object.class);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }
}