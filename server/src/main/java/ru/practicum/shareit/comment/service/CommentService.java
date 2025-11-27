package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}