package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

/**
 * Сервис для управления бронированием в системе ShareIt.
 * <p>
 * Обеспечивает полную бизнес-логику по работе с бронированием:
 * создание, подтверждение, просмотр, список.
 * </p>
 *
 * @see Booking
 * @see BookingDto
 * @see BookingRepository
 */
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
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Нельзя забронировать собственную вещь");
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException("Некорректные даты бронирования");
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
            throw new NotFoundException("Бронирование не найдено");
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
        userService.getUserById(bookerId); // валидация существования
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state == null ? "ALL" : state) {
            case "ALL" -> bookingRepository.findByBookerId(bookerId, sort);
            case "CURRENT" -> bookingRepository.findByBookerId(bookerId, sort).stream()
                    .filter(b -> b.getStart().isBefore(LocalDateTime.now()) && b.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            case "PAST" -> bookingRepository.findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), sort);
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), sort);
            case "WAITING" -> filterByStatus(bookingRepository.findByBookerId(bookerId, sort), WAITING);
            case "REJECTED" -> filterByStatus(bookingRepository.findByBookerId(bookerId, sort), REJECTED);
            default -> throw new ValidationException("Unknown state: " + state);
        };
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        userService.getUserById(ownerId); // валидация существования
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state == null ? "ALL" : state) {
            case "ALL" -> bookingRepository.findByItemOwnerId(ownerId, sort);
            case "CURRENT" -> bookingRepository.findByItemOwnerId(ownerId, sort).stream()
                    .filter(b -> b.getStart().isBefore(LocalDateTime.now()) && b.getEnd().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            case "PAST" -> bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, LocalDateTime.now(), sort);
            case "FUTURE" -> bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, LocalDateTime.now(), sort);
            case "WAITING" -> filterByStatus(bookingRepository.findByItemOwnerId(ownerId, sort), WAITING);
            case "REJECTED" -> filterByStatus(bookingRepository.findByItemOwnerId(ownerId, sort), REJECTED);
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