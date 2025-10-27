package ru.practicum.shareit.exception;

import lombok.*;

/**
 * DTO для ответа при возникновении ошибки.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
}