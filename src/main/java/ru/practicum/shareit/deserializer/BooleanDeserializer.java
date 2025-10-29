package ru.practicum.shareit.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Кастомный десериализатор для преобразования строковых значений в {@link Boolean}.
 * Используется для корректной обработки JSON-полей, где булевы значения могут передаваться
 * как строки (например, {@code "available": "false"}).
 */
public class BooleanDeserializer extends JsonDeserializer<Boolean> {

    /**
     * Десериализует строковое значение из JSON в объект типа {@link Boolean}.
     *
     * @param p    парсер JSON, предоставляющий текущее значение
     * @param ctxt контекст десериализации
     * @return {@code Boolean} значение или {@code null}, если входное значение пустое
     * @throws IOException если возникла ошибка при чтении JSON
     */
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}