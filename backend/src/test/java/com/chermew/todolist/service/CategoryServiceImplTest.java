package com.chermew.todolist.service;

import com.chermew.todolist.dto.CategoryRequest;
import com.chermew.todolist.dto.CategoryResponse;
import com.chermew.todolist.entity.Category;
import com.chermew.todolist.entity.Project;
import com.chermew.todolist.entity.Todo;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.repository.CategoryRepository;
import com.chermew.todolist.repository.ProjectMemberRepository;
import com.chermew.todolist.repository.ProjectRepository;
import com.chermew.todolist.repository.TodoRepository;
import com.chermew.todolist.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID userId;
    private Project project;
    private Category category;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        project = Project.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .ownerId(userId)
                .build();

        category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .name("Sprint 1")
                .description("Sprint description")
                .colorCode("#FF5733")
                .assignedTo(userId)
                .build();
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = CategoryRequest.builder()
                .projectId(project.getId())
                .name("Sprint 1")
                .description("Sprint description")
                .colorCode("#FF5733")
                .assignedTo(userId)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(categoryRepository.existsByProjectIdAndName(project.getId(), "Sprint 1")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request, userId);

        assertNotNull(response);
        assertEquals("Sprint 1", response.getName());
        assertEquals(userId, response.getAssignedTo());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_AccessDenied_ThrowsException() {
        CategoryRequest request = CategoryRequest.builder()
                .projectId(project.getId())
                .name("Sprint 1")
                .build();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(IllegalArgumentException.class, () -> 
                categoryService.createCategory(request, UUID.randomUUID())
        );
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        CategoryRequest request = CategoryRequest.builder()
                .projectId(project.getId())
                .name("Sprint 1")
                .build();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(categoryRepository.existsByProjectIdAndName(project.getId(), "Sprint 1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
                categoryService.createCategory(request, userId)
        );
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Sprint")
                .description("Updated desc")
                .colorCode("#000000")
                .assignedTo(userId)
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByProjectIdAndName(project.getId(), "Updated Sprint")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.updateCategory(category.getId(), request, userId);

        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_AccessDenied_ThrowsException() {
        CategoryRequest request = CategoryRequest.builder().name("Updated").build();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class, () -> 
                categoryService.updateCategory(category.getId(), request, UUID.randomUUID())
        );
    }

    @Test
    void updateCategory_DuplicateName_ThrowsException() {
        CategoryRequest request = CategoryRequest.builder().name("Duplicate").build();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByProjectIdAndName(project.getId(), "Duplicate")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
                categoryService.updateCategory(category.getId(), request, userId)
        );
    }

    @Test
    void getCategoryById_AsOwner_Success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(category.getId(), userId);

        assertNotNull(response);
        assertEquals("Sprint 1", response.getName());
    }

    @Test
    void getCategoryById_AsMember_Success() {
        UUID memberId = UUID.randomUUID();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);

        CategoryResponse response = categoryService.getCategoryById(category.getId(), memberId);

        assertNotNull(response);
    }

    @Test
    void getCategoryById_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                categoryService.getCategoryById(category.getId(), nonMemberId)
        );
    }

    @Test
    void deleteCategory_Success_AsOwner() {
        Todo todo = Todo.builder()
                .id(UUID.randomUUID())
                .category(category)
                .project(project)
                .status(TodoStatus.COMPLETED) // Owner can delete even if completed
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(todo));

        categoryService.deleteCategory(category.getId(), userId);

        verify(todoRepository, times(1)).delete(todo);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_Success_AsMember_NoCompletedTodos() {
        UUID memberId = UUID.randomUUID();
        Todo todo = Todo.builder()
                .id(UUID.randomUUID())
                .category(category)
                .project(project)
                .status(TodoStatus.PENDING)
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(todo));

        categoryService.deleteCategory(category.getId(), memberId);

        verify(todoRepository, times(1)).delete(todo);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_Failure_AsMember_WithCompletedTodo() {
        UUID memberId = UUID.randomUUID();
        Todo todo = Todo.builder()
                .id(UUID.randomUUID())
                .category(category)
                .project(project)
                .status(TodoStatus.COMPLETED) // Member cannot delete if completed
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(todo));

        assertThrows(IllegalArgumentException.class, () ->
                categoryService.deleteCategory(category.getId(), memberId)
        );
    }

    @Test
    void deleteCategory_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                categoryService.deleteCategory(category.getId(), nonMemberId)
        );
    }
}
