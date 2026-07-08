package com.chermew.todolist.dto;

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
public class CategoryResponse {
    private String name;
    private String colorCode;

    private UUID id;
    private UUID projectId;
    private String description;
    private UUID assignedTo;
    private OffsetDateTime createdAt;
}
