package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceTest {

    private ItemService itemService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        itemService = new ItemService(userService);
    }

    @Test
    @DisplayName("Должен создавать новую вещь для существующего пользователя")
    void shouldCreateItem() {
        UserDto userDto = new UserDto(null, "Alice", "alice@example.com");
        UserDto owner = userService.create(userDto);

        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true);
        ItemDto saved = itemService.create(owner.getId(), itemDto);

        assertNotNull(saved.getId());
        assertEquals("Drill", saved.getName());
        assertEquals("Powerful drill", saved.getDescription());
        assertTrue(saved.getAvailable());
    }

    @Test
    @DisplayName("Должен выбрасывать NotFoundException при создании вещи для несуществующего пользователя")
    void shouldThrowNotFoundWhenCreatingItemForNonExistentUser() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true);
        assertThrows(NotFoundException.class, () -> itemService.create(999L, itemDto));
    }

    @Test
    @DisplayName("Должен находить вещи по тексту в названии или описании (только доступные)")
    void shouldSearchItemsByText() {
        UserDto user = userService.create(new UserDto(null, "Alice", "alice@example.com"));
        itemService.create(user.getId(), new ItemDto(null, "Drill", "Powerful drill", true));
        itemService.create(user.getId(), new ItemDto(null, "Hammer", "Steel hammer", true));
        itemService.create(user.getId(), new ItemDto(null, "Saw", "Hand saw", false)); // недоступен

        List<ItemDto> results = itemService.search("drill");

        assertEquals(1, results.size());
        assertEquals("Drill", results.get(0).getName());
    }

    @Test
    @DisplayName("Должен возвращать пустой список при поиске недоступных вещей")
    void shouldReturnEmptyListWhenSearchingUnavailableItems() {
        UserDto user = userService.create(new UserDto(null, "Alice", "alice@example.com"));
        itemService.create(user.getId(), new ItemDto(null, "Drill", "Powerful drill", false));

        List<ItemDto> results = itemService.search("drill");

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Должен выбрасывать NotFoundException при попытке обновить чужую вещь")
    void shouldThrowNotFoundWhenUpdatingItemByNonOwner() {
        UserDto owner = userService.create(new UserDto(null, "Owner", "owner@example.com"));
        UserDto other = userService.create(new UserDto(null, "Other", "other@example.com"));

        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true);
        ItemDto saved = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto(null, "New Drill", "Updated", true);

        assertThrows(NotFoundException.class, () -> itemService.update(other.getId(), saved.getId(), updateDto));
    }
}