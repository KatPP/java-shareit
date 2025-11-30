package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.HeaderValidationException;

import java.util.List;

/**
 * REST-контроллер для управления бронированиями в системе ShareIt.
 * Предоставляет эндпоинты для создания, подтверждения, получения и просмотра бронирований.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Метод парсит заголовок X-Sharer-User-Id в Long userId.
     * Вызывается перед каждым методом, использующим @ModelAttribute("userId").
     */
    @ModelAttribute("userId")
    public Long parseUserIdHeader(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader) {
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
    public BookingResponseDto create(@ModelAttribute("userId") Long userId,
                                     @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@ModelAttribute("userId") Long userId,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto get(@ModelAttribute("userId") Long userId,
                                  @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@ModelAttribute("userId") Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@ModelAttribute("userId") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByOwner(userId, state);
    }
}