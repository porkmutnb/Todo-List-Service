package com.chermew.todolist.aspect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryActivity {
    private String tableName;
    private String action; // SAVE, DELETE, etc.
    private String entityId;
}
