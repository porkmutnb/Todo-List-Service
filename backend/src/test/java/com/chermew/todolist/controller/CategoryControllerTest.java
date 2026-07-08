package com.chermew.todolist.controller;

import com.chermew.todolist.dto.CategoryRequest;
import com.chermew.todolist.dto.CategoryResponse;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.CategoryService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private UUID mockUserId;
    private UserPrincipal userPrincipal;
    private CategoryResponse categoryResponse;

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

        categoryResponse = CategoryResponse.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .name("Sprint 1")
                .description("Desc")
                .colorCode("#FF5733")
                .assignedTo(mockUserId)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .setControllerAdvice(new com.chermew.todolist.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCategory_Success() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .projectId(categoryResponse.getProjectId())
                .name("Sprint 1")
                .description("Desc")
                .colorCode("#FF5733")
                .assignedTo(mockUserId)
                .build();

        when(categoryService.createCategory(any(CategoryRequest.class), eq(mockUserId)))
                .thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sprint 1"));
    }

    @Test
    void updateCategory_Success() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Sprint")
                .description("Updated desc")
                .colorCode("#000000")
                .assignedTo(mockUserId)
                .build();

        categoryResponse.setName("Updated Sprint");
        categoryResponse.setDescription("Updated desc");
        categoryResponse.setColorCode("#000000");

        when(categoryService.updateCategory(eq(categoryResponse.getId()), any(CategoryRequest.class), eq(mockUserId)))
                .thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/categories/{categoryId}", categoryResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Sprint"));
    }

    @Test
    void getCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById(categoryResponse.getId(), mockUserId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sprint 1"));
    }

    @Test
    void deleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory(categoryResponse.getId(), mockUserId);

        mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}
