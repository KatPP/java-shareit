package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

/**
 * REST-контроллер для пользователей в ShareIt Gateway.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable long userId, @RequestBody UserDto userDto) {
        return userClient.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable long userId) {
        return userClient.get(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        return userClient.delete(userId);
    }
}