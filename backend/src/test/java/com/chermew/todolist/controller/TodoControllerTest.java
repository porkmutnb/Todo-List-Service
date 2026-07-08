package com.chermew.todolist.controller;

import com.chermew.todolist.dto.TodoRequest;
import com.chermew.todolist.dto.TodoResponse;
import com.chermew.todolist.enums.TodoPriority;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private UUID mockUserId;
    private UserPrincipal userPrincipal;
    private TodoResponse todoResponse;

    private final HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return userPrincipal;
        }
    };

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        userPrincipal = UserPrincipal.builder()
                .id(mockUserId)
                .email("user@example.com")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        todoResponse = TodoResponse.builder()
                .id(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .title("Test Todo")
                .description("Desc")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.MEDIUM)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .setControllerAdvice(new com.chermew.todolist.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTodo_Success() throws Exception {
        TodoRequest request = TodoRequest.builder()
                .projectId(todoResponse.getProjectId())
                .categoryId(todoResponse.getCategoryId())
                .title("Test Todo")
                .description("Desc")
                .status(TodoStatus.PENDING)
                .priority(TodoPriority.MEDIUM)
                .build();

        when(todoService.createTodo(any(TodoRequest.class), eq(mockUserId)))
                .thenReturn(todoResponse);

        mockMvc.perform(post("/api/v1/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Todo"));
    }

    @Test
    void updateTodo_Success() throws Exception {
        TodoRequest request = TodoRequest.builder()
                .title("Updated Todo")
                .description("Updated desc")
                .status(TodoStatus.IN_PROGRESS)
                .priority(TodoPriority.HIGH)
                .build();

        todoResponse.setTitle("Updated Todo");
        todoResponse.setStatus(TodoStatus.IN_PROGRESS);
        todoResponse.setPriority(TodoPriority.HIGH);

        when(todoService.updateTodo(eq(todoResponse.getId()), any(TodoRequest.class), eq(mockUserId)))
                .thenReturn(todoResponse);

        mockMvc.perform(put("/api/v1/todos/{todoId}", todoResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Todo"));
    }

    @Test
    void getTodoById_Success() throws Exception {
        when(todoService.getTodoById(todoResponse.getId(), mockUserId)).thenReturn(todoResponse);

        mockMvc.perform(get("/api/v1/todos/{todoId}", todoResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Todo"));
    }

    @Test
    void getTodosByCategory_Success() throws Exception {
        List<TodoResponse> responses = Collections.singletonList(todoResponse);
        when(todoService.getTodosByCategory(todoResponse.getCategoryId(), mockUserId)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/todos/{categoryId}/todos", todoResponse.getCategoryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Test Todo"));
    }

    @Test
    void deleteTodo_Success() throws Exception {
        doNothing().when(todoService).deleteTodo(todoResponse.getId(), mockUserId);

        mockMvc.perform(delete("/api/v1/todos/{todoId}", todoResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todo deleted successfully"));
    }
}
