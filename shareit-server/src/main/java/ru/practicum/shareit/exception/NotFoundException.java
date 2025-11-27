package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при отсутствии сущности (пользователь, вещь и т.д.).
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}