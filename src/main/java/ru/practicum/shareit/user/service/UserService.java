package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Сервис для управления пользователями в системе ShareIt.
 * Реализует основные операции: создание, обновление, получение и удаление пользователей.
 * Хранение данных осуществляется в памяти (in-memory), без использования базы данных.
 */
@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    /**
     * Создаёт нового пользователя на основе переданного DTO.
     *
     * @param userDto данные нового пользователя (имя и email)
     * @return созданный пользователь в формате DTO
     * @throws ValidationException если имя или email не прошли валидацию
     * @throws ConflictException   если пользователь с таким email уже существует
     */
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

    /**
     * Обновляет данные существующего пользователя по его идентификатору.
     * Можно обновлять только имя, только email или оба поля одновременно.
     * Если email изменяется, проверяется его уникальность.
     *
     * @param userId  идентификатор пользователя
     * @param userDto DTO с новыми данными (имя и/или email)
     * @return обновлённый пользователь в формате DTO
     * @throws NotFoundException   если пользователь с указанным ID не найден
     * @throws ValidationException если email не соответствует формату или пуст
     * @throws ConflictException   если новый email уже используется другим пользователем
     */
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserById(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();

        if (newEmail != null) {
            validateEmail(newEmail);
            if (!newEmail.equals(existingUser.getEmail()) && isEmailExists(newEmail)) {
                throw new ConflictException("Email " + newEmail + " уже используется");
            }
        }

        if (newName != null) existingUser.setName(newName);
        if (newEmail != null) existingUser.setEmail(newEmail);

        return UserMapper.toUserDto(existingUser);
    }

    /**
     * Возвращает пользователя по его идентификатору.
     * @param userId идентификатор пользователя
     * @return пользователь в формате DTO
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(getUserById(userId));
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    public void delete(Long userId) {
        users.remove(userId);
    }

    /**
     * Внутренний метод для получения модели пользователя по ID.
     * Используется внутри сервиса.
     *
     * @param userId идентификатор пользователя
     * @return модель пользователя
     * @throws NotFoundException если пользователь не найден
     */
    public User getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }

    /**
     * Проверяет корректность данных пользователя при создании.
     *
     * @param userDto данные пользователя
     * @throws ValidationException если имя или email пусты или некорректны
     */
    private void validateUser(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("Данные пользователя не могут быть null");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        validateEmail(userDto.getEmail());
    }

    /**
     * Проверяет корректность email-адреса по регулярному выражению.
     *
     * @param email email для проверки
     * @throws ValidationException если email пуст или не соответствует формату
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }

    /**
     * Проверяет, существует ли пользователь с указанным email (регистронезависимо).
     *
     * @param email email для проверки
     * @return {@code true}, если такой email уже используется, иначе {@code false}
     */
    private boolean isEmailExists(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}