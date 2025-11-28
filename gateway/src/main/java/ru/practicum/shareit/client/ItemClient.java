package ru.practicum.shareit.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(RestTemplateBuilder builder) {
        super(builder);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> create(Long userId, Object itemDto) {
        return getRestTemplate().exchange(
                API_PREFIX,
                org.springframework.http.HttpMethod.POST,
                createEntityWithHeader(itemDto, userId),
                Object.class
        );
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, Object itemDto) {
        return getRestTemplate().exchange(
                API_PREFIX + "/" + itemId,
                org.springframework.http.HttpMethod.PATCH,
                createEntityWithHeader(itemDto, userId),
                Object.class
        );
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        var headers = new org.springframework.http.HttpHeaders();
        if (userId != null) {
            headers.set("X-Sharer-User-Id", userId.toString());
        }
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX + "/" + itemId,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getOwnerItems(Long userId) {
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

    public ResponseEntity<Object> search(String text) {
        return getRestTemplate().getForEntity(API_PREFIX + "/search?text=" + text, Object.class);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, Object commentDto) {
        return getRestTemplate().exchange(
                API_PREFIX + "/" + itemId + "/comment",
                org.springframework.http.HttpMethod.POST,
                createEntityWithHeader(commentDto, userId),
                Object.class
        );
    }
}