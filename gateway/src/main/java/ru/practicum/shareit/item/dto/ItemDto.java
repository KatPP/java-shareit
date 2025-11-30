package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.deserializer.BooleanDeserializer;

/**
 * DTO для передачи данных вещи через REST API.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @JsonDeserialize(using = BooleanDeserializer.class)
    private Boolean available;

    private Long requestId;
}