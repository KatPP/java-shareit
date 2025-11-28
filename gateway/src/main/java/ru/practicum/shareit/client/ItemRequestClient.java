package ru.practicum.shareit.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(RestTemplateBuilder builder) {
        super(builder);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> create(Long userId, Object requestDto) {
        return getRestTemplate().exchange(
                API_PREFIX,
                org.springframework.http.HttpMethod.POST,
                createEntityWithHeader(requestDto, userId),
                Object.class
        );
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getAll(Long userId, int from, int size) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                String.format("%s/all?from=%d&size=%d", API_PREFIX, from, size),
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX + "/" + requestId,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }
}