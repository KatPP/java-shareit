package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemResponseMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления вещами (items) в системе ShareIt.
 * <p>
 * Отвечает за выполнение бизнес-логики, связанной с созданием,
 * обновлением, поиском и просмотром вещей. Вся работа с хранилищем данных
 * делегируется репозиторию {@link ItemRepository}.
 * </p>
 *
 * @see Item
 * @see ItemDto
 * @see ItemRepository
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    /**
     * Создаёт новую вещь для указанного пользователя.
     * <p>
     * Владельцем вещи становится пользователь с указанным ID.
     * Вещь должна иметь название, описание и статус доступности.
     * </p>
     *
     * @param userId   идентификатор владельца вещи
     * @param itemDto  данные новой вещи
     * @return созданная вещь в формате {@link ItemDto}
     * @throws ValidationException если данные вещи не прошли валидацию
     * @throws NotFoundException   если пользователь с указанным ID не найден
     */
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        validateItemDto(itemDto);
        User owner = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    /**
     * Обновляет данные существующей вещи.
     * <p>
     * Обновление может включать любое подмножество полей: {@code name},
     * {@code description}, {@code available}. Обновлять вещь может только её владелец.
     * </p>
     *
     * @param userId   идентификатор пользователя, пытающегося обновить вещь
     * @param itemId   идентификатор обновляемой вещи
     * @param itemDto  DTO с полями для обновления (остальные поля могут быть null)
     * @return обновлённая вещь в формате {@link ItemDto}
     * @throws NotFoundException   если вещь не найдена или пользователь не является её владельцем
     * @throws ValidationException если переданы некорректные данные
     */
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    /**
     * Возвращает данные вещи по её идентификатору с информацией о бронировании и отзывами.
     * <p>
     * Заголовок {@code X-Sharer-User-Id} не требуется для этого метода.
     * Но если он присутствует, может использоваться в будущем для дополнительной логики.
     * </p>
     *
     * @param itemId идентификатор вещи
     * @param userId идентификатор запрашивающего пользователя (может быть null)
     * @return расширенный DTO вещи с бронированиями и комментариями
     * @throws NotFoundException если вещь не найдена
     */
    @Transactional(readOnly = true)
    public ItemResponseDto getByIdWithBookingsAndComments(Long itemId, Long userId) {
        Item item = getItemById(itemId);
        return ItemResponseMapper.toItemResponseDto(item, bookingRepository, commentRepository);
    }

    /**
     * Возвращает список всех вещей указанного владельца с датами бронирования и отзывами.
     *
     * @param userId идентификатор владельца
     * @return список расширенных DTO вещей
     * @throws NotFoundException если владелец не найден
     */
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getOwnerItemsWithBookingsAndComments(Long userId) {
        userService.getUserById(userId); // проверка существования
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(item -> ItemResponseMapper.toItemResponseDto(item, bookingRepository, commentRepository))
                .collect(Collectors.toList());
    }

    /**
     * Выполняет поиск вещей по текстовому запросу в названии или описании.
     * <p>
     * В результаты включаются только доступные для аренды вещи ({@code available = true}).
     * Поиск регистронезависимый.
     * </p>
     *
     * @param text текст поискового запроса
     * @return список подходящих вещей в формате {@link ItemDto}
     */
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Добавляет новый комментарий к вещи.
     * <p>
     * Комментарий может оставить только пользователь, у которого:
     * <ul>
     *   <li>было завершённое бронирование этой вещи,</li>
     *   <li>статус бронирования — APPROVED,</li>
     *   <li>текущее время — после окончания срока аренды.</li>
     * </ul>
     * </p>
     *
     * @param userId   идентификатор автора комментария
     * @param itemId   идентификатор вещи
     * @param commentDto данные комментария (текст)
     * @return созданный комментарий в формате {@link CommentDto}
     * @throws ValidationException если пользователь не имеет права оставлять комментарий
     * @throws NotFoundException   если вещь не найдена
     */
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userService.getUserById(userId);
        Item item = getItemById(itemId);

        boolean hasApprovedBooking = bookingRepository.existsByItemIdAndBookerIdAndEndIsBefore(
                itemId, userId, LocalDateTime.now()
        );

        if (!hasApprovedBooking) {
            throw new ValidationException("Нельзя оставить отзыв без завершённого бронирования");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    /**
     * Возвращает модель вещи по её идентификатору.
     * <p>
     * Используется внутри других сервисов (например, в BookingService).
     * </p>
     *
     * @param id идентификатор вещи
     * @return объект модели {@link Item}
     * @throws NotFoundException если вещь не найдена
     */
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + id + " не найдена"));
    }

    /**
     * Проверяет корректность данных вещи при создании.
     *
     * @param itemDto данные вещи
     * @throws ValidationException если название, описание или статус доступности отсутствуют
     */
    private void validateItemDto(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("Данные вещи не могут быть null");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности (available) обязателен");
        }
    }
}