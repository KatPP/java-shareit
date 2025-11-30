package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto create(Long bookerId, BookingDto bookingDto) {
        if (bookerId == null || bookerId <= 0) {
            throw new ValidationException("Некорректный идентификатор пользователя");
        }
        if (bookingDto == null) {
            throw new ValidationException("Данные бронирования не могут быть null");
        }
        var item = itemService.getItemById(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нельзя забронировать собственную вещь");
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Некорректные даты бронирования");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Дата начала бронирования не может быть в прошлом");
        }
        if (bookingDto.getStart() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Дата начала бронирования обязательна");
        }
        if (bookingDto.getEnd() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Дата окончания бронирования обязательна");
        }
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(userService.getUserById(bookerId));
        booking.setStatus(WAITING);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Недостаточно прав для подтверждения бронирования");
        }
        if (booking.getStatus() != WAITING) {
            throw new ValidationException("Нельзя подтвердить/отклонить уже обработанное бронирование");
        }
        booking.setStatus(approved ? APPROVED : REJECTED);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не найдено");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long bookerId, String state) {
        userService.getUserById(bookerId);
        var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state == null ? "ALL" : state) {
            case "ALL" -> bookingRepository.findByBooker_Id(bookerId, sort);
            case "CURRENT" -> bookingRepository.findByBooker_Id(bookerId, sort).stream()
                    .filter(b -> b.getStart().isBefore(LocalDateTime.now()) && b.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            case "PAST" -> bookingRepository.findByBooker_IdAndEndIsBefore(bookerId, LocalDateTime.now(), sort);
            case "FUTURE" -> bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, LocalDateTime.now(), sort);
            case "WAITING" -> filterByStatus(bookingRepository.findByBooker_Id(bookerId, sort), WAITING);
            case "REJECTED" -> filterByStatus(bookingRepository.findByBooker_Id(bookerId, sort), REJECTED);
            default -> throw new ValidationException("Unknown state: " + state);
        };
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        userService.getUserById(ownerId);
        var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state == null ? "ALL" : state) {
            case "ALL" -> bookingRepository.findByItem_Owner_Id(ownerId, sort);
            case "CURRENT" -> bookingRepository.findByItem_Owner_Id(ownerId, sort).stream()
                    .filter(b -> b.getStart().isBefore(LocalDateTime.now()) && b.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            case "PAST" -> bookingRepository.findByItem_Owner_IdAndEndIsBefore(ownerId, LocalDateTime.now(), sort);
            case "FUTURE" -> bookingRepository.findByItem_Owner_IdAndStartIsAfter(ownerId, LocalDateTime.now(), sort);
            case "WAITING" -> filterByStatus(bookingRepository.findByItem_Owner_Id(ownerId, sort), WAITING);
            case "REJECTED" -> filterByStatus(bookingRepository.findByItem_Owner_Id(ownerId, sort), REJECTED);
            default -> throw new ValidationException("Unknown state: " + state);
        };
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + id + " не найдено"));
    }

    private List<Booking> filterByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).collect(Collectors.toList());
    }
}