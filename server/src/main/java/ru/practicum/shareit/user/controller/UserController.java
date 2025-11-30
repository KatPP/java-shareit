package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        userDto.setId(userId);
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}