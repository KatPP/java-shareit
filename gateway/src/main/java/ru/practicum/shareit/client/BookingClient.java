package ru.practicum.shareit.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplateBuilder builder) {
        super(builder);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> create(Long userId, Object bookingDto) {
        return getRestTemplate().exchange(
                API_PREFIX,
                org.springframework.http.HttpMethod.POST,
                createEntityWithHeader(bookingDto, userId),
                Object.class
        );
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        String url = String.format("%s/%d?approved=%s", API_PREFIX, bookingId, approved);
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                url,
                org.springframework.http.HttpMethod.PATCH,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX + "/" + bookingId,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getAllByBooker(Long userId, String state) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX + "?state=" + state,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        var entity = new org.springframework.http.HttpEntity<>(headers);
        return getRestTemplate().exchange(
                API_PREFIX + "/owner?state=" + state,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object.class
        );
    }
}