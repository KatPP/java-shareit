package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void addComment_shouldFailIfNoFinishedBooking() {
        // Given
        UserDto user = new UserDto(null, "Commentator", "commentator@test.com");
        UserDto savedUser = userService.create(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Профессиональная");
        itemDto.setAvailable(true);
        ItemDto savedItem = itemService.create(savedUser.getId(), itemDto);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная вещь!");

        // When & Then
        assertThrows(ru.practicum.shareit.exception.ValidationException.class, () -> {
            commentService.addComment(savedUser.getId(), savedItem.getId(), commentDto);
        });
    }
}