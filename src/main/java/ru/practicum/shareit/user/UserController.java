package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

/**
 * REST-контроллер для управления пользователями в системе ShareIt.
 * Предоставляет эндпоинты для создания, обновления, получения и удаления пользователей.
 * Все операции выполняются через {@link UserService}.
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Конструктор для внедрения зависимости сервиса пользователей.
     *
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param userDto данные нового пользователя (имя и email)
     * @return созданный пользователь с присвоенным ID
     * @throws ConflictException   если пользователь с таким email уже существует
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto);
        UserDto saved = userService.create(userDto);
        log.info("Пользователь создан: {}", saved);
        return saved;
    }

    /**
     * Частично обновляет данные существующего пользователя по его ID.
     * Можно обновлять только имя, только email или оба поля.
     * Если email изменяется, проверяется его уникальность.
     *
     * @param userId  идентификатор пользователя
     * @param userDto DTO с полями для обновления (остальные поля могут быть null)
     * @return обновлённый пользователь
     * @throws NotFoundException   если пользователь с указанным ID не найден
     * @throws ConflictException   если новый email уже используется другим пользователем
     */
    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        log.info("Обновление пользователя ID={}: {}", userId, userDto);
        UserDto updated = userService.update(userId, userDto);
        log.info("Пользователь обновлён: {}", updated);
        return updated;
    }

    /**
     * Возвращает данные пользователя по его идентификатору.
     * @param userId идентификатор пользователя
     * @return данные пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получение пользователя по ID: {}", userId);
        UserDto user = userService.getById(userId);
        log.info("Пользователь найден: {}", user);
        return user;
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Удаление пользователя ID={}", userId);
        userService.delete(userId);
        log.info("Пользователь ID={} удалён", userId);
    }
}