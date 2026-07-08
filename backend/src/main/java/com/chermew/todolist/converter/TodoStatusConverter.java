package com.chermew.todolist.converter;

import com.chermew.todolist.enums.TodoStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TodoStatusConverter implements AttributeConverter<TodoStatus, String> {

    @Override
    public String convertToDatabaseColumn(TodoStatus attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public TodoStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? TodoStatus.fromValue(dbData) : null;
    }
}
