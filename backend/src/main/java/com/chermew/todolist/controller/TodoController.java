package com.chermew.todolist.controller;

import com.chermew.todolist.annotation.LogActivity;
import com.chermew.todolist.dto.ApiResponse;
import com.chermew.todolist.dto.TodoRequest;
import com.chermew.todolist.dto.TodoResponse;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    @LogActivity(action = "TODO_CREATE", entityType = "todos")
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        TodoResponse response = todoService.createTodo(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Todo created successfully", response));
    }

    @PutMapping("/{todoId}")
    @LogActivity(action = "TODO_UPDATE", entityType = "todos")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @PathVariable UUID todoId,
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        TodoResponse response = todoService.updateTodo(todoId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Todo updated successfully", response));
    }

    @GetMapping("/{todoId}")
    @LogActivity(action = "TODO_GET_BY_ID", entityType = "todos")
    public ResponseEntity<ApiResponse<TodoResponse>> getTodoById(
            @PathVariable UUID todoId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        TodoResponse response = todoService.getTodoById(todoId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Todo fetched successfully", response));
    }

    @GetMapping("/{categoryId}/todos")
    @LogActivity(action = "TODO_GET_BY_CATEGORY", entityType = "todos")
    public ResponseEntity<ApiResponse<List<TodoResponse>>> getTodosByCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<TodoResponse> response = todoService.getTodosByCategory(categoryId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Todos fetched successfully", response));
    }

    @DeleteMapping("/{todoId}")
    @LogActivity(action = "TODO_DELETE", entityType = "todos")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @PathVariable UUID todoId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        todoService.deleteTodo(todoId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Todo deleted successfully", null));
    }
}
