package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BookingClient;
import ru.practicum.shareit.exception.HeaderValidationException;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new HeaderValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long id = Long.parseLong(userIdHeader);
            if (id <= 0) {
                throw new HeaderValidationException("Некорректный идентификатор пользователя");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new HeaderValidationException("Некорректный идентификатор пользователя");
        }
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                         @RequestBody Object bookingDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                          @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                      @PathVariable Long bookingId) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingClient.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                                @RequestParam(defaultValue = "ALL") String state) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingClient.getAllByOwner(userId, state);
    }
}