package ru.practicum.shareit.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для всех REST-контроллеров приложения ShareIt.
 * Перехватывает стандартные и кастомные исключения, логирует их и возвращает
 * клиенту понятные сообщения об ошибках в формате JSON с соответствующим HTTP-статусом.
 */
@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    /**
     * Обрабатывает исключения валидации, возникающие при нарушении бизнес-правил
     * (например, пустое имя или некорректный email).
     * @param e исключение валидации
     * @return объект с сообщением об ошибке
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает исключения валидации, возникающие при нарушении ограничений,
     * заданных аннотациями валидации (например, {@code @NotBlank}, {@code @Email})
     * в DTO-объектах при использовании {@code @Valid}.
     * @param e исключение валидации аргументов метода контроллера
     * @return объект с первым найденным сообщением об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации");
        log.error("Ошибка валидации: {}", message);
        return new ErrorResponse(message);
    }

    /**
     * Обрабатывает конфликты данных, например, попытку создания пользователя
     * с уже существующим email.
     * @param e исключение конфликта
     * @return объект с сообщением об ошибке
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e) {
        log.error("Конфликт: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает ситуации, когда запрашиваемая сущность (пользователь, вещь и т.д.)
     * не найдена в системе.
     * @param e исключение "не найдено"
     * @return объект с сообщением об ошибке
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.error("Не найдено: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает все остальные непредвиденные исключения.
     * В целях безопасности клиенту возвращается общее сообщение,
     * а полная информация об ошибке логируется на сервере.
     * @param e любое неперехваченное исключение
     * @return объект с общим сообщением об ошибке
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e) {
        log.error("Внутренняя ошибка сервера", e);
        return new ErrorResponse("Произошла внутренняя ошибка сервера");
    }

    @ExceptionHandler(HeaderValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHeaderValidation(HeaderValidationException e) {
        log.error("Ошибка валидации заголовка: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }
}