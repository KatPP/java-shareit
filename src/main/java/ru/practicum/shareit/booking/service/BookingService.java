package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long bookerId, BookingDto bookingDto);
    BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approved);
    BookingResponseDto getById(Long userId, Long bookingId);
    List<BookingResponseDto> getAllByBooker(Long bookerId, String state);
    List<BookingResponseDto> getAllByOwner(Long ownerId, String state);
}