package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
        return userService.create(userDto);
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
        return userService.update(userId, userDto);
    }

    /**
     * Возвращает данные пользователя по его идентификатору.
     * @param userId идентификатор пользователя
     * @return данные пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}