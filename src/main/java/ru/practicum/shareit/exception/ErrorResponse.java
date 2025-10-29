package ru.practicum.shareit.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO для ответа при возникновении ошибки.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    String error;
}