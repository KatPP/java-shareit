package ru.practicum.shareit.comment.mapper;

import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.dto.CommentDto;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}