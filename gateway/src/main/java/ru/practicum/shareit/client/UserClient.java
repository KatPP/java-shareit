package ru.practicum.shareit.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplateBuilder builder;
    @Value("${shareit.server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(Object userDto) {
        return getRestTemplate().postForEntity("/users", userDto, Object.class);
    }

    public ResponseEntity<Object> update(Long userId, Object userDto) {
        return getRestTemplate().exchange(
                "/users/" + userId,
                org.springframework.http.HttpMethod.PATCH,
                new HttpEntity<>(userDto),
                Object.class
        );
    }

    public ResponseEntity<Object> getById(Long userId) {
        return getRestTemplate().getForEntity("/users/" + userId, Object.class);
    }

    public ResponseEntity<Object> delete(Long userId) {
        getRestTemplate().delete("/users/" + userId);
        return ResponseEntity.noContent().build();
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }
}