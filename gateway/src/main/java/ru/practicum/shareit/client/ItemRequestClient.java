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
public class ItemRequestClient {
    private final RestTemplateBuilder builder;
    @Value("${shareit.server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(Long userId, Object requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(requestDto, headers);
        return getRestTemplate().postForEntity("/requests", request, Object.class);
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/requests", org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> getAll(Long userId, int from, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = String.format("/requests/all?from=%d&size=%d", from, size);
        return getRestTemplate().exchange(url, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/requests/" + requestId, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }
}