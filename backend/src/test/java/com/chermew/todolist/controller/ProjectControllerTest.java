package com.chermew.todolist.controller;

import com.chermew.todolist.dto.DataByProjectResponse;
import com.chermew.todolist.dto.ProjectRequest;
import com.chermew.todolist.dto.ProjectResponse;
import com.chermew.todolist.enums.ProjectRole;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.ProjectService;
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
public class ProjectControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private UUID mockUserId;
    private UserPrincipal userPrincipal;
    private ProjectResponse projectResponse;

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

        projectResponse = ProjectResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .description("Test Description")
                .ownerId(mockUserId)
                .role(ProjectRole.OWNER)
                .status("pending")
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .setControllerAdvice(new com.chermew.todolist.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void createProject_Success() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("Test Project")
                .description("Test Description")
                .build();

        when(projectService.createProject(any(ProjectRequest.class), eq(mockUserId))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Project"));
    }

    @Test
    void updateProject_Success() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("Updated Project")
                .description("Updated Description")
                .build();

        projectResponse.setTitle("Updated Project");
        projectResponse.setDescription("Updated Description");

        when(projectService.updateProject(eq(projectResponse.getId()), any(ProjectRequest.class), eq(mockUserId)))
                .thenReturn(projectResponse);

        mockMvc.perform(put("/api/v1/projects/{id}", projectResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Project"));
    }

    @Test
    void getAllProjects_Success() throws Exception {
        List<ProjectResponse> responses = Collections.singletonList(projectResponse);
        when(projectService.getAllProjects(mockUserId)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Test Project"));
    }

    @Test
    void getProjectById_Success() throws Exception {
        when(projectService.getProjectById(projectResponse.getId(), mockUserId)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/v1/projects/{id}", projectResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(projectResponse.getId().toString()));
    }

    @Test
    void getCategoriesByProject_Success() throws Exception {
        DataByProjectResponse catResponse = DataByProjectResponse.builder()
                .id(UUID.randomUUID())
                .projectId(projectResponse.getId())
                .name("Sprint 1")
                .description("Desc")
                .colorCode("#FF5733")
                .assignedTo(mockUserId)
                .build();

        List<DataByProjectResponse> responses = Collections.singletonList(catResponse);
        when(projectService.getCategoriesByProject(projectResponse.getId(), mockUserId)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/projects/{projectId}/categories", projectResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Sprint 1"));
    }

    @Test
    void deleteProject_Success() throws Exception {
        doNothing().when(projectService).deleteProject(projectResponse.getId(), mockUserId);

        mockMvc.perform(delete("/api/v1/projects/{projectId}", projectResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));
    }
}
