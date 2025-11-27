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
public class BookingClient {
    private final RestTemplateBuilder builder;
    @Value("${shareit.server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(Long bookerId, Object bookingDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(bookerId));
        HttpEntity<Object> request = new HttpEntity<>(bookingDto, headers);
        return getRestTemplate().postForEntity("/bookings", request, Object.class);
    }

    public ResponseEntity<Object> approve(Long ownerId, Long bookingId, Boolean approved) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(ownerId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange(
                "/bookings/" + bookingId + "?approved=" + approved,
                org.springframework.http.HttpMethod.PATCH,
                request,
                Object.class
        );
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/bookings/" + bookingId, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> getAllByBooker(Long bookerId, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(bookerId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/bookings?state=" + state, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> getAllByOwner(Long ownerId, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(ownerId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return getRestTemplate().exchange("/bookings/owner?state=" + state, org.springframework.http.HttpMethod.GET, request, Object.class);
    }

    private org.springframework.web.client.RestTemplate getRestTemplate() {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }
}