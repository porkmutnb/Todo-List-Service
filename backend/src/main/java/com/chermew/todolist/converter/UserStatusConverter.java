package com.chermew.todolist.converter;

import com.chermew.todolist.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? UserStatus.fromValue(dbData) : null;
    }
}
