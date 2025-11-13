package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.regex.Pattern;

/**
 * Сервис для управления пользователями в системе ShareIt.
 * Отвечает за выполнение бизнес-логики, связанной с созданием, обновлением,
 * получением и удалением пользователей. Вся работа с хранилищем данных
 * делегируется репозиторию {@link UserRepository}.
 * Все операции, изменяющие состояние данных (создание, обновление, удаление),
 * помечены аннотацией {@link Transactional} с режимом по умолчанию —
 * транзакция открывается и завершается автоматически.
 *
 * @see User
 * @see UserDto
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * Репозиторий для выполнения операций сущности {@link User} в базе данных.
     */
    private final UserRepository userRepository;

    /**
     * Регулярное выражение для валидации формата email-адреса.
     * Поддерживает стандартный формат email без международных доменов.
     * Примеры корректных адресов: {@code user@example.com}, {@code test+tag@sub.domain.org}
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    /**
     * Создаёт нового пользователя на основе переданного DTO.
     * Перед сохранением выполняется валидация данных и проверка уникальности email.
     * В случае нарушения бизнес-правил выбрасываются соответствующие исключения.
     *
     * @param userDto объект с данными нового пользователя (имя и email)
     * @return созданный пользователь в формате {@link UserDto} с присвоенным ID
     * @throws ValidationException если имя или email не соответствуют требованиям
     * @throws ConflictException   если пользователь с таким email уже существует в системе
     */
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

    /**
     * Обновляет данные существующего пользователя по его идентификатору.
     * Возможна частичная замена полей: можно обновить только имя, только email
     * или оба поля одновременно. Если email изменяется, проверяется его уникальность.
     * Пользователь не может получить email, уже занятый другим пользователем.
     *
     * @param userId  идентификатор обновляемого пользователя
     * @param userDto объект с новыми данными (имя и/или email)
     * @return обновлённый пользователь в формате {@link UserDto}
     * @throws NotFoundException   если пользователь с указанным ID не найден
     * @throws ValidationException если email не соответствует формату или пуст
     * @throws ConflictException   если новый email уже используется другим пользователем
     */
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserById(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();

        if (newEmail != null) {
            validateEmail(newEmail);
            if (!newEmail.equalsIgnoreCase(existingUser.getEmail()) &&
                    userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new ConflictException("Email " + newEmail + " уже используется");
            }
            existingUser.setEmail(newEmail);
        }

        if (newName != null) {
            existingUser.setName(newName);
        }

        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    /**
     * Возвращает пользователя по его идентификатору.
     * Запрос делегируется репозиторию. Если пользователь не найден,
     * выбрасывается исключение {@link NotFoundException}.
     *
     * @param userId идентификатор запрашиваемого пользователя
     * @return пользователь в формате {@link UserDto}
     * @throws NotFoundException если пользователь с указанным ID отсутствует в базе
     */
    public UserDto getById(Long userId) {
        User user = getUserById(userId);
        return UserMapper.toUserDto(user);
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * Если пользователь не существует, метод завершается без ошибки
     * (в соответствии с поведением {@link org.springframework.data.repository.CrudRepository#deleteById}).
     *
     * @param userId идентификатор удаляемого пользователя
     */
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Возвращает модель пользователя по его идентификатору.
     *
     * Используется внутри других сервисов для получения полной сущности {@link User},
     * например, при создании вещи или бронирования.
     *
     * @param userId идентификатор пользователя
     * @return объект модели {@link User}
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    /**
     * Проверяет корректность данных пользователя при создании.
     * Убеждается, что объект не null, имя и email не пустые.
     *
     * @param userDto данные пользователя для валидации
     * @throws ValidationException если данные не проходят валидацию
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
     * Email не может быть null, пустым или содержать недопустимые символы.
     * @param email email для проверки
     * @throws ValidationException если email не соответствует формату
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }
}