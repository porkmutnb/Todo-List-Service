package com.chermew.todolist.service.impl;

import com.chermew.todolist.dto.TodoRequest;
import com.chermew.todolist.dto.TodoResponse;
import com.chermew.todolist.entity.Category;
import com.chermew.todolist.entity.Project;
import com.chermew.todolist.entity.Todo;
import com.chermew.todolist.enums.TodoPriority;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.repository.CategoryRepository;
import com.chermew.todolist.repository.ProjectMemberRepository;
import com.chermew.todolist.repository.ProjectRepository;
import com.chermew.todolist.repository.TodoRepository;
import com.chermew.todolist.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public TodoResponse createTodo(TodoRequest request, UUID userId) {
        UUID categoryId = request.getCategoryId();
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        }

        if(request.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + request.getProjectId()));

        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You do not have permission to add todos in this project");
        }

        Todo todo = Todo.builder()
                .category(category)
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TodoStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : TodoPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .assignedTo(request.getAssignedTo())
                .createdBy(userId)
                .build();

        Todo savedTodo = todoRepository.save(todo);
        return mapToResponse(savedTodo);
    }

    @Override
    @Transactional
    public TodoResponse updateTodo(UUID todoId, TodoRequest request, UUID userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + todoId));

        Project project = todo.getProject();
        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You do not have permission to update todos in this project");
        }

        UUID categoryId = request.getCategoryId();
        if(categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
            todo.setCategory(category);
        }

        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getStatus() != null) todo.setStatus(request.getStatus());
        if (request.getPriority() != null) todo.setPriority(request.getPriority());
        if (request.getDueDate() != null) todo.setDueDate(request.getDueDate());
        if (request.getAssignedTo() != null) todo.setAssignedTo(request.getAssignedTo());

        Todo updatedTodo = todoRepository.save(todo);
        return mapToResponse(updatedTodo);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getTodoById(UUID todoId, UUID userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + todoId));

        Project project = todo.getProject();
        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this todo");
        }

        return mapToResponse(todo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        Project project = category.getProject();
        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this category's todos");
        }

        List<Todo> todos = todoRepository.findByCategoryId(categoryId);
        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTodo(UUID todoId, UUID userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + todoId));
        Project project = todo.getProject();

        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this category's todos");
        }

        if(isMember && TodoStatus.COMPLETED.equals(todo.getStatus())) {
            throw new IllegalArgumentException("This todo is completed and cannot be deleted");
        }

        todoRepository.delete(todo);
    }

    private TodoResponse mapToResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .categoryId(todo.getCategory() != null ? todo.getCategory().getId() : null)
                .projectId(todo.getProject().getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus())
                .priority(todo.getPriority())
                .dueDate(todo.getDueDate())
                .assignedTo(todo.getAssignedTo())
                .createdBy(todo.getCreatedBy())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
