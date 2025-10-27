package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для управления пользователями с in-memory хранилищем.
 */
@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserDto create(UserDto userDto) {
        validateUser(userDto);
        if (isEmailExists(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserById(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();

        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            if (isEmailExists(newEmail)) {
                throw new ConflictException("Email " + newEmail + " уже используется");
            }
        }

        if (newName != null) existingUser.setName(newName);
        if (newEmail != null) existingUser.setEmail(newEmail);

        return UserMapper.toUserDto(existingUser);
    }

    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(getUserById(userId));
    }

    public void delete(Long userId) {
        users.remove(userId);
    }

    public User getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }

    private void validateUser(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("Данные пользователя не могут быть null");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
    }

    private boolean isEmailExists(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}