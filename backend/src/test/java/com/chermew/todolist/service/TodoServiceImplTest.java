package com.chermew.todolist.service;

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
import com.chermew.todolist.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

    private UUID userId;
    private Project project;
    private Category category;
    private Todo todo;

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
                .build();

        todo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(category)
                .title("Complete backend")
                .description("Desc")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.MEDIUM)
                .dueDate(OffsetDateTime.now())
                .assignedTo(userId)
                .createdBy(userId)
                .build();
    }

    @Test
    void createTodo_AsOwner_Success() {
        TodoRequest request = TodoRequest.builder()
                .projectId(project.getId())
                .categoryId(category.getId())
                .title("Complete backend")
                .description("Desc")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.MEDIUM)
                .dueDate(todo.getDueDate())
                .assignedTo(userId)
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        TodoResponse response = todoService.createTodo(request, userId);

        assertNotNull(response);
        assertEquals("Complete backend", response.getTitle());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void createTodo_AsMember_Success() {
        UUID memberId = UUID.randomUUID();
        TodoRequest request = TodoRequest.builder()
                .projectId(project.getId())
                .categoryId(category.getId())
                .title("Complete backend")
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        TodoResponse response = todoService.createTodo(request, memberId);

        assertNotNull(response);
    }

    @Test
    void createTodo_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        TodoRequest request = TodoRequest.builder()
                .projectId(project.getId())
                .categoryId(category.getId())
                .title("Complete backend")
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                todoService.createTodo(request, nonMemberId)
        );
    }

    @Test
    void updateTodo_Success() {
        TodoRequest request = TodoRequest.builder()
                .title("Updated Title")
                .description("Updated desc")
                .status(TodoStatus.IN_PROGRESS)
                .priority(TodoPriority.HIGH)
                .dueDate(OffsetDateTime.now())
                .assignedTo(userId)
                .build();

        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        TodoResponse response = todoService.updateTodo(todo.getId(), request, userId);

        assertNotNull(response);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void updateTodo_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        TodoRequest request = TodoRequest.builder().title("Updated Title").build();

        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                todoService.updateTodo(todo.getId(), request, nonMemberId)
        );
    }

    @Test
    void getTodoById_Success() {
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));

        TodoResponse response = todoService.getTodoById(todo.getId(), userId);

        assertNotNull(response);
    }

    @Test
    void getTodoById_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                todoService.getTodoById(todo.getId(), nonMemberId)
        );
    }

    @Test
    void getTodosByCategory_Success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(todo));

        List<TodoResponse> responses = todoService.getTodosByCategory(category.getId(), userId);

        assertEquals(1, responses.size());
    }

    @Test
    void getTodosByCategory_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                todoService.getTodosByCategory(category.getId(), nonMemberId)
        );
    }

    @Test
    void deleteTodo_Success_AsOwner() {
        Todo completedTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .status(TodoStatus.COMPLETED) // Owner can delete completed todo
                .build();

        when(todoRepository.findById(completedTodo.getId())).thenReturn(Optional.of(completedTodo));

        todoService.deleteTodo(completedTodo.getId(), userId);

        verify(todoRepository, times(1)).delete(completedTodo);
    }

    @Test
    void deleteTodo_Success_AsMember_NotCompleted() {
        UUID memberId = UUID.randomUUID();
        Todo pendingTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .status(TodoStatus.PENDING)
                .build();

        when(todoRepository.findById(pendingTodo.getId())).thenReturn(Optional.of(pendingTodo));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);

        todoService.deleteTodo(pendingTodo.getId(), memberId);

        verify(todoRepository, times(1)).delete(pendingTodo);
    }

    @Test
    void deleteTodo_Failure_AsMember_Completed() {
        UUID memberId = UUID.randomUUID();
        Todo completedTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .status(TodoStatus.COMPLETED)
                .build();

        when(todoRepository.findById(completedTodo.getId())).thenReturn(Optional.of(completedTodo));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                todoService.deleteTodo(completedTodo.getId(), memberId)
        );
    }

    @Test
    void deleteTodo_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                todoService.deleteTodo(todo.getId(), nonMemberId)
        );
    }
}
