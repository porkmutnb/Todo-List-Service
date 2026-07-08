package com.chermew.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.chermew.todolist.enums.TodoPriority;
import com.chermew.todolist.enums.TodoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {

    private UUID projectId;

    private UUID categoryId;

    @NotBlank(message = "Todo title is required")
    @Size(max = 255, message = "Todo title must not exceed 255 characters")
    private String title;

    private String description;

    private TodoStatus status;

    private TodoPriority priority;

    private OffsetDateTime dueDate;

    private UUID assignedTo;
}
