package ru.practicum.shareit.user.service;


import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public interface UserService {
    UserDto create(UserDto userDto);
    UserDto update(Long userId, UserDto userDto);
    UserDto getById(Long userId);
    void delete(Long userId);
    // Добавьте метод parseUserIdHeader, если он нужен в контроллере
    Long parseUserIdHeader(String userIdHeader);

    User getUserById(Long userId);
}