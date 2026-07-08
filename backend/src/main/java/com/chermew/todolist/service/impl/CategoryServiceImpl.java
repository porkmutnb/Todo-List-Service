package com.chermew.todolist.service.impl;

import com.chermew.todolist.dto.*;
import com.chermew.todolist.entity.Category;
import com.chermew.todolist.entity.Project;
import com.chermew.todolist.entity.Todo;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.repository.CategoryRepository;
import com.chermew.todolist.repository.ProjectMemberRepository;
import com.chermew.todolist.repository.ProjectRepository;
import com.chermew.todolist.repository.TodoRepository;
import com.chermew.todolist.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, UUID userId) {
        UUID projectId = request.getProjectId();
        if(projectId == null) {
            throw new RuntimeException("Project ID is required");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Security check: Only project owner can create categories
        if (!project.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Only the project owner can create categories");
        }

        // Unique name check per project
        if (categoryRepository.existsByProjectIdAndName(projectId, request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists in this project");
        }

        Category category = Category.builder()
                .project(project)
                .name(request.getName())
                .description(request.getDescription())
                .colorCode(request.getColorCode())
                .assignedTo(request.getAssignedTo())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryRequest request, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        Project project = category.getProject();
        // Security check: Only project owner can update categories
        if (!project.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Only the project owner can update categories");
        }

        // Unique name check if name has changed
        if (!category.getName().equalsIgnoreCase(request.getName()) &&
                categoryRepository.existsByProjectIdAndName(project.getId(), request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists in this project");
        }

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getColorCode() != null) category.setColorCode(request.getColorCode());
        if (request.getAssignedTo() != null) category.setAssignedTo(request.getAssignedTo());

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        Project project = category.getProject();
        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this category");
        }

        return mapToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        Project project = category.getProject();
        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this project's categories");
        }

        todoRepository.findByCategoryId(categoryId).forEach( todo -> {
            if(isMember && TodoStatus.COMPLETED.equals(todo.getStatus())) {
                throw new IllegalArgumentException("This todo for this category is completed and cannot be deleted");
            }
            todoRepository.delete(todo);
        });

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .projectId(category.getProject().getId())
                .name(category.getName())
                .description(category.getDescription())
                .colorCode(category.getColorCode())
                .assignedTo(category.getAssignedTo())
                .createdAt(category.getCreatedAt())
                .build();
    }

}
