package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при конфликте (например, дубликат email).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}