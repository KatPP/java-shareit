package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.regex.Pattern;

/**
 * Сервис для управления пользователями в системе ShareIt.
 * <p>
 * Отвечает за выполнение бизнес-логики, связанной с созданием,
 * обновлением, получением и удалением пользователей. Вся работа с хранилищем данных
 * делегируется репозиторию {@link UserRepository}.
 * </p>
 *
 * @see User
 * @see UserDto
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // ДОБАВИТЬ
public class UserService {

    private final UserRepository userRepository;

    /**
     * Регулярное выражение для валидации формата email-адреса.
     * <p>
     * Поддерживает стандартный формат email без международных доменов.
     * Примеры корректных адресов: {@code user@example.com}, {@code test+tag@sub.domain.org}
     * </p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    /**
     * Создаёт нового пользователя на основе переданного DTO.
     * <p>
     * Перед сохранением выполняется валидация данных и проверка уникальности email.
     * В случае нарушения бизнес-правил выбрасываются соответствующие исключения.
     * </p>
     *
     * @param userDto объект с данными нового пользователя (имя и email)
     * @return созданный пользователь в формате {@link UserDto} с присвоенным ID
     * @throws ValidationException если имя или email не прошли валидацию
     * @throws ConflictException   если пользователь с таким email уже существует в системе
     */
    @Transactional
    public UserDto create(UserDto userDto) {
        System.out.println("=== DEBUG: UserService.create() START ===");
        System.out.println("Received UserDto: " + userDto);

        try {
            // 1. Валидация
            System.out.println("DEBUG: Starting validation...");
            validateUser(userDto);
            System.out.println("DEBUG: Validation passed");

            // 2. Проверка существования пользователя
            System.out.println("DEBUG: Checking if user exists...");
            boolean exists = userRepository.existsByEmailIgnoreCase(userDto.getEmail());
            System.out.println("DEBUG: User exists: " + exists);

            if (exists) {
                System.out.println("DEBUG: User already exists, throwing ConflictException");
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }

            // 3. Маппинг
            System.out.println("DEBUG: Mapping UserDto to User...");
            User user = UserMapper.toUser(userDto);
            System.out.println("DEBUG: Mapped User: " + user);

            // 4. Сохранение
            System.out.println("DEBUG: Saving user to database...");
            User savedUser = userRepository.save(user);
            System.out.println("DEBUG: Saved user with ID: " + savedUser.getId());

            // 5. Маппинг обратно
            UserDto result = UserMapper.toUserDto(savedUser);
            System.out.println("DEBUG: Returning UserDto: " + result);
            System.out.println("=== DEBUG: UserService.create() SUCCESS ===");

            return result;

        } catch (Exception e) {
            System.out.println("=== DEBUG: UserService.create() ERROR ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== DEBUG: END ===");
            throw e;
        }
    }

    /**
     * Обновляет данные существующего пользователя по его идентификатору.
     * <p>
     * Возможна частичная замена полей: можно обновить только имя, только email
     * или оба поля одновременно. Если email изменяется, проверяется его уникальность.
     * Пользователь не может получить email, уже занятый другим пользователем.
     * </p>
     *
     * @param userId  идентификатор обновляемого пользователя
     * @param userDto DTO с новыми данными (имя и/или email)
     * @return обновлённый пользователь в формате {@link UserDto}
     * @throws NotFoundException   если пользователь с указанным ID не найден
     * @throws ValidationException если email не соответствует формату или пуст
     * @throws ConflictException   если новый email уже используется другим пользователем
     */
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

    /**
     * Возвращает пользователя по его идентификатору.
     * <p>
     * Запрос делегируется репозиторию. Если пользователь не найден,
     * выбрасывается исключение {@link NotFoundException}.
     * </p>
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
     * <p>
     * Если пользователь не существует, метод завершается без ошибки
     * (в соответствии с поведением {@link org.springframework.data.repository.CrudRepository#deleteById}).
     * </p>
     *
     * @param userId идентификатор удаляемого пользователя
     */
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Возвращает модель пользователя по его идентификатору.
     * <p>
     * Используется внутри других сервисов для получения полной сущности {@link User},
     * например, при создании вещи или бронирования.
     * </p>
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
     * Парсит и валидирует значение заголовка {@code X-Sharer-User-Id}.
     *
     * @param userIdHeader строковое значение заголовка
     * @return идентификатор пользователя как {@code Long}
     * @throws ValidationException если заголовок отсутствует, пуст, не является числом
     *                                                или содержит недопустимое значение (≤ 0)
     */
    public Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            if (userId <= 0) {
                throw new ValidationException("Некорректный идентификатор пользователя");
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new ValidationException("Некорректный идентификатор пользователя");
        }
    }

    /**
     * Проверяет корректность данных пользователя при создании.
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
     * <p>
     * Email не может быть null, пустым или содержать недопустимые символы.
     * </p>
     *
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