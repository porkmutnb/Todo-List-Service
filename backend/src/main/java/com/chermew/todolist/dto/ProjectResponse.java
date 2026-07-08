package com.chermew.todolist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.chermew.todolist.enums.ProjectRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private UUID ownerId;
    private ProjectRole role; // "owner" or "member"
    private String status;
    private List<UUID> memberList;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
