package com.chermew.todolist.converter;

import com.chermew.todolist.enums.ProjectRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectRoleConverter implements AttributeConverter<ProjectRole, String> {

    @Override
    public String convertToDatabaseColumn(ProjectRole attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public ProjectRole convertToEntityAttribute(String dbData) {
        return dbData != null ? ProjectRole.fromValue(dbData) : null;
    }
}
