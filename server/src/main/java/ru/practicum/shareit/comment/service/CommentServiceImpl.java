package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

/**
 * Сервис для работы с комментариями.
 * Позволяет добавлять комментарий к вещи только после завершенного и подтвержденного бронирования.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userService.getUserById(userId);
        Item item = getItemById(itemId);
        boolean hasApprovedAndFinishedBooking = bookingRepository.existsByItemIdAndBookerIdAndEndIsBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), APPROVED
        );
        if (!hasApprovedAndFinishedBooking) {
            throw new ValidationException("Нельзя оставить отзыв без завершённого бронирования");
        }
        Comment comment = new Comment();
        String text = (commentDto != null && commentDto.getText() != null) ? commentDto.getText() : "";
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Item getItemById(Long id) {
        return itemService.getItemById(id);
    }
}