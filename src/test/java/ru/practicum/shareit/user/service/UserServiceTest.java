package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    @DisplayName("Должен создавать нового пользователя")
    void shouldCreateUser() {
        UserDto userDto = new UserDto(null, "Alice", "alice@example.com");
        UserDto saved = userService.create(userDto);

        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение ConflictException при попытке создать пользователя с уже существующим email")
    void createDuplicateEmail_throwsConflict() {
        UserDto user1 = new UserDto(null, "Alice", "alice@example.com");
        UserDto user2 = new UserDto(null, "Bob", "alice@example.com");

        userService.create(user1);

        assertThrows(ConflictException.class, () -> userService.create(user2));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение NotFoundException при попытке получить несуществующего пользователя")
    void getById_nonExistent_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    @DisplayName("Должен обновлять email пользователя")
    void updateEmail_updatesUser() {
        UserDto userDto = new UserDto(null, "Alice", "alice@example.com");
        UserDto saved = userService.create(userDto);

        UserDto updated = new UserDto(null, "Alice", "alice.new@example.com");
        UserDto result = userService.update(saved.getId(), updated);

        assertEquals("alice.new@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Должен выбрасывать ConflictException при обновлении пользователя на email другого пользователя")
    void updateToExistingEmail_throwsConflict() {
        UserDto user1 = new UserDto(null, "Alice", "alice@example.com");
        UserDto user2 = new UserDto(null, "Bob", "bob@example.com");

        UserDto saved1 = userService.create(user1);
        UserDto saved2 = userService.create(user2);

        UserDto updateDto = new UserDto(null, "Alice Updated", "bob@example.com");

        assertThrows(ConflictException.class, () -> userService.update(saved1.getId(), updateDto));
    }
}