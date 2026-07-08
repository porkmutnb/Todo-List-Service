package com.chermew.todolist.service;

import com.chermew.todolist.dto.TodoRequest;
import com.chermew.todolist.dto.TodoResponse;

import java.util.List;
import java.util.UUID;

public interface TodoService {
    TodoResponse createTodo(TodoRequest request, UUID userId);
    TodoResponse updateTodo(UUID todoId, TodoRequest request, UUID userId);
    TodoResponse getTodoById(UUID todoId, UUID userId);
    List<TodoResponse> getTodosByCategory(UUID categoryId, UUID userId);
    void deleteTodo(UUID todoId, UUID userId);
}
