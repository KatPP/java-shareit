package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldCreateAndReturnUser() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("New User");
        userDto.setEmail("newuser@test.com");

        // When
        UserDto result = userService.create(userDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    void createUser_withExistingEmail_shouldThrowConflictException() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("User One");
        userDto.setEmail("existing@test.com");
        userService.create(userDto);

        UserDto duplicateUserDto = new UserDto();
        duplicateUserDto.setName("User Two");
        duplicateUserDto.setEmail("existing@test.com");

        // When & Then
        assertThrows(ConflictException.class, () -> {
            userService.create(duplicateUserDto);
        });
    }
}