package ru.practicum.shareit.item.mapper;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;

public class ItemResponseMapper {

    public static ItemResponseDto toItemResponseDto(Item item,
                                                    BookingRepository bookingRepository,
                                                    CommentRepository commentRepository) {
        var last = bookingRepository.findLastBookingsByItemId(item.getId(), PageRequest.of(0, 1));
        var next = bookingRepository.findNextBookingsByItemId(item.getId(), PageRequest.of(0, 1));
        var comments = commentRepository.findByItemId(item.getId());

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                last.isEmpty() ? null : BookingMapper.toBookingResponseDto(last.get(0)),
                next.isEmpty() ? null : BookingMapper.toBookingResponseDto(next.get(0)),
                comments.isEmpty() ? Collections.emptyList() :
                        comments.stream().map(CommentMapper::toCommentDto).toList()
        );
    }
}