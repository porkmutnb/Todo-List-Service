package com.chermew.todolist.repository;

import com.chermew.todolist.entity.Todo;
import com.chermew.todolist.enums.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    List<Todo> findByProjectId(UUID projectId);
    List<Todo> findByCategoryId(UUID categoryId);
    long countByProjectId(UUID projectId);
    long countByProjectIdAndStatusNot(UUID projectId, TodoStatus status);
}
