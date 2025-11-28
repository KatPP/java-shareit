package ru.practicum.shareit.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(RestTemplateBuilder builder) {
        super(builder);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> create(Object userDto) {
        return getRestTemplate().postForEntity(API_PREFIX, userDto, Object.class);
    }

    public ResponseEntity<Object> update(Long userId, Object userDto) {
        return getRestTemplate().exchange(
                API_PREFIX + "/" + userId,
                org.springframework.http.HttpMethod.PATCH,
                mapToHttpEntity(userDto),
                Object.class
        );
    }

    public ResponseEntity<Object> getById(Long userId) {
        return getRestTemplate().getForEntity(API_PREFIX + "/" + userId, Object.class);
    }

    public ResponseEntity<Object> delete(Long userId) {
        getRestTemplate().delete(API_PREFIX + "/" + userId);
        return ResponseEntity.noContent().build();
    }
}