package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.HeaderValidationException;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new HeaderValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            if (userId <= 0) {
                throw new HeaderValidationException("Некорректный идентификатор пользователя");
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new HeaderValidationException("Некорректный идентификатор пользователя");
        }
    }

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                     @RequestBody BookingDto bookingDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto get(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                  @PathVariable Long bookingId) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        Long userId = parseUserIdHeader(userIdHeader);
        return bookingService.getAllByOwner(userId, state);
    }
}