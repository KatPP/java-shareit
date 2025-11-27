package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;

    @Test
    void createRequest_shouldCreateAndReturnRequest() {
        // Given
        UserDto user = new UserDto(null, "Requestor", "requestor@test.com");
        UserDto savedUser = userService.create(user);

        // When
        ItemRequestDto requestDto = itemRequestService.create(savedUser.getId(), "Нужна дрель");

        // Then
        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getId()).isNotNull();
        assertThat(requestDto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(requestDto.getItems()).isEmpty();
    }

}