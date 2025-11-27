package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при отсутствии или некорректности заголовка X-Sharer-User-Id.
 */
public class HeaderValidationException extends RuntimeException {
    public HeaderValidationException(String message) {
        super(message);
    }
}