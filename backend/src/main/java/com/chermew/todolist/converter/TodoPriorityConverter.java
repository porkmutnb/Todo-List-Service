package com.chermew.todolist.converter;

import com.chermew.todolist.enums.TodoPriority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TodoPriorityConverter implements AttributeConverter<TodoPriority, String> {

    @Override
    public String convertToDatabaseColumn(TodoPriority attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public TodoPriority convertToEntityAttribute(String dbData) {
        return dbData != null ? TodoPriority.fromValue(dbData) : null;
    }
}
