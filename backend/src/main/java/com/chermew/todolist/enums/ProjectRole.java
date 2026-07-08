package com.chermew.todolist.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectRole {
    OWNER("owner"),
    MEMBER("member");

    private final String value;

    ProjectRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProjectRole fromValue(String value) {
        for (ProjectRole role : ProjectRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown project role: " + value);
    }
}
