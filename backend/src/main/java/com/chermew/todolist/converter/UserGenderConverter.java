package com.chermew.todolist.converter;

import com.chermew.todolist.enums.UserGender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserGenderConverter implements AttributeConverter<UserGender, String> {

    @Override
    public String convertToDatabaseColumn(UserGender attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public UserGender convertToEntityAttribute(String dbData) {
        return dbData != null ? UserGender.fromValue(dbData) : null;
    }
}
