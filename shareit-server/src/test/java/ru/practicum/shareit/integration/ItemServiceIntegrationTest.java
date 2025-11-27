package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @Test
    void createItem_shouldCreateAndReturnItem() {
        // Given
        UserDto user = new UserDto(null, "Owner", "owner@test.com");
        UserDto savedUser = userService.create(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Профессиональная");
        itemDto.setAvailable(true);

        // When
        ItemDto result = itemService.create(savedUser.getId(), itemDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Профессиональная");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getOwnerItemsWithBookingsAndComments_shouldReturnItemsWithEmptyBookingsAndComments() {
        // Given
        UserDto user = new UserDto(null, "Owner", "owner@test.com");
        UserDto savedUser = userService.create(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Профессиональная");
        itemDto.setAvailable(true);
        ItemDto savedItem = itemService.create(savedUser.getId(), itemDto);

        // When
        List<ItemResponseDto> items = itemService.getOwnerItemsWithBookingsAndComments(savedUser.getId());

        // Then
        assertThat(items).hasSize(1);
        ItemResponseDto item = items.get(0);
        assertThat(item.getLastBooking()).isNull();
        assertThat(item.getNextBooking()).isNull();
        assertThat(item.getComments()).isEmpty();
    }
}