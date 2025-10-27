package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto);
        UserDto saved = userService.create(userDto);
        log.info("Пользователь создан: {}", saved);
        return saved;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        log.info("Обновление пользователя ID={}: {}", userId, userDto);
        UserDto updated = userService.update(userId, userDto);
        log.info("Пользователь обновлён: {}", updated);
        return updated;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получение пользователя по ID: {}", userId);
        UserDto user = userService.getById(userId);
        log.info("Пользователь найден: {}", user);
        return user;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Удаление пользователя ID={}", userId);
        userService.delete(userId);
        log.info("Пользователь ID={} удалён", userId);
    }
}