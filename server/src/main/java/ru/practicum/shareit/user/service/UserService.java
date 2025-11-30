package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.regex.Pattern;

/**
 * Сервис для управления пользователями в системе ShareIt.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    @Transactional
    public UserDto create(UserDto userDto) {
        validateUser(userDto);
        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User user = getUserById(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();

        if (newEmail != null) {
            validateEmail(newEmail);
            if (!newEmail.equalsIgnoreCase(user.getEmail()) &&
                    userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new ConflictException("Email " + newEmail + " уже используется");
            }
            user.setEmail(newEmail);
        }
        if (newName != null) {
            user.setName(newName);
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public UserDto getById(Long userId) {
        User user = getUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateUser(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("Данные пользователя не могут быть null");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        validateEmail(userDto.getEmail());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }
}