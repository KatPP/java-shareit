package ru.practicum.shareit.item.mapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.List;

/**
 * Класс для преобразования модели {@link Item} в расширенный DTO {@link ItemResponseDto},
 * содержащий информацию о последнем и следующем бронировании, а также отзывы.
 */
public class ItemResponseMapper {

    /**
     * Преобразует модель вещи в расширенный DTO, содержащий информацию о бронировании и отзывах.
     *
     * @param item               модель вещи
     * @param bookingRepository  репозиторий бронирований
     * @param commentRepository  репозиторий комментариев
     * @return расширенный DTO вещи
     */
    public static ItemResponseDto toItemResponseDto(Item item,
                                                    BookingRepository bookingRepository,
                                                    CommentRepository commentRepository) {
        // Для последних бронирований: сортировка по end DESC (самые новые сначала)
        List<BookingResponseDto> lastBookings = bookingRepository
                .findLastBookingsByItemId(item.getId(),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "end")))
                .stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();

        // Для следующих бронирований: сортировка по start ASC (самые ближайшие сначала)
        List<BookingResponseDto> nextBookings = bookingRepository
                .findNextBookingsByItemId(item.getId(),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "start")))
                .stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();

        List<CommentDto> comments = commentRepository.findByItemId(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBookings.isEmpty() ? null : lastBookings.get(0),
                nextBookings.isEmpty() ? null : nextBookings.get(0),
                comments.isEmpty() ? Collections.emptyList() : comments
        );
    }
}