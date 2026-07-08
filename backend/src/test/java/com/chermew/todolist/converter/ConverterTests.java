package com.chermew.todolist.converter;

import com.chermew.todolist.enums.ProjectRole;
import com.chermew.todolist.enums.TodoPriority;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.enums.UserGender;
import com.chermew.todolist.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTests {

    @Test
    void testUserGenderConverter() {
        UserGenderConverter converter = new UserGenderConverter();
        
        assertEquals("male", converter.convertToDatabaseColumn(UserGender.MALE));
        assertEquals("female", converter.convertToDatabaseColumn(UserGender.FEMALE));
        assertNull(converter.convertToDatabaseColumn(null));

        assertEquals(UserGender.MALE, converter.convertToEntityAttribute("male"));
        assertEquals(UserGender.FEMALE, converter.convertToEntityAttribute("female"));
        assertNull(converter.convertToEntityAttribute(null));

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }

    @Test
    void testTodoStatusConverter() {
        TodoStatusConverter converter = new TodoStatusConverter();

        assertEquals("pending", converter.convertToDatabaseColumn(TodoStatus.PENDING));
        assertEquals("in_progress", converter.convertToDatabaseColumn(TodoStatus.IN_PROGRESS));
        assertEquals("completed", converter.convertToDatabaseColumn(TodoStatus.COMPLETED));
        assertNull(converter.convertToDatabaseColumn(null));

        assertEquals(TodoStatus.PENDING, converter.convertToEntityAttribute("pending"));
        assertEquals(TodoStatus.IN_PROGRESS, converter.convertToEntityAttribute("in_progress"));
        assertEquals(TodoStatus.COMPLETED, converter.convertToEntityAttribute("completed"));
        assertNull(converter.convertToEntityAttribute(null));

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }

    @Test
    void testTodoPriorityConverter() {
        TodoPriorityConverter converter = new TodoPriorityConverter();

        assertEquals("low", converter.convertToDatabaseColumn(TodoPriority.LOW));
        assertEquals("medium", converter.convertToDatabaseColumn(TodoPriority.MEDIUM));
        assertEquals("high", converter.convertToDatabaseColumn(TodoPriority.HIGH));
        assertNull(converter.convertToDatabaseColumn(null));

        assertEquals(TodoPriority.LOW, converter.convertToEntityAttribute("low"));
        assertEquals(TodoPriority.MEDIUM, converter.convertToEntityAttribute("medium"));
        assertEquals(TodoPriority.HIGH, converter.convertToEntityAttribute("high"));
        assertNull(converter.convertToEntityAttribute(null));

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }

    @Test
    void testProjectRoleConverter() {
        ProjectRoleConverter converter = new ProjectRoleConverter();

        assertEquals("owner", converter.convertToDatabaseColumn(ProjectRole.OWNER));
        assertEquals("member", converter.convertToDatabaseColumn(ProjectRole.MEMBER));
        assertNull(converter.convertToDatabaseColumn(null));

        assertEquals(ProjectRole.OWNER, converter.convertToEntityAttribute("owner"));
        assertEquals(ProjectRole.MEMBER, converter.convertToEntityAttribute("member"));
        assertNull(converter.convertToEntityAttribute(null));

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }

    @Test
    void testUserStatusConverter() {
        UserStatusConverter converter = new UserStatusConverter();

        assertEquals("active", converter.convertToDatabaseColumn(UserStatus.ACTIVE));
        assertEquals("inactive", converter.convertToDatabaseColumn(UserStatus.INACTIVE));
        assertNull(converter.convertToDatabaseColumn(null));

        assertEquals(UserStatus.ACTIVE, converter.convertToEntityAttribute("active"));
        assertEquals(UserStatus.INACTIVE, converter.convertToEntityAttribute("inactive"));
        assertNull(converter.convertToEntityAttribute(null));

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
}
