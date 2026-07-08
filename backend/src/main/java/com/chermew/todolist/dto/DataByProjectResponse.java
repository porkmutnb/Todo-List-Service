package com.chermew.todolist.dto;

import com.chermew.todolist.enums.TodoPriority;
import com.chermew.todolist.enums.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataByProjectResponse {
    private UUID id;
    private UUID projectId;
    private String description;
    private String type;
    private UUID assignedTo;
    private OffsetDateTime createdAt;

    private String name;
    private String colorCode;

    private UUID categoryId;
    private String title;
    private TodoStatus status;
    private TodoPriority priority;
    private OffsetDateTime dueDate;
    private UUID createdBy;
    private OffsetDateTime updatedAt;
}
