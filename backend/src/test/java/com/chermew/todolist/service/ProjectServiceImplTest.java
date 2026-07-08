package com.chermew.todolist.service;

import com.chermew.todolist.dto.DataByProjectResponse;
import com.chermew.todolist.dto.ProjectRequest;
import com.chermew.todolist.dto.ProjectResponse;
import com.chermew.todolist.entity.Category;
import com.chermew.todolist.entity.Project;
import com.chermew.todolist.entity.ProjectMember;
import com.chermew.todolist.entity.Todo;
import com.chermew.todolist.entity.User;
import com.chermew.todolist.enums.ProjectRole;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.repository.CategoryRepository;
import com.chermew.todolist.repository.ProjectMemberRepository;
import com.chermew.todolist.repository.ProjectRepository;
import com.chermew.todolist.repository.TodoRepository;
import com.chermew.todolist.repository.UserRepository;
import com.chermew.todolist.service.impl.ProjectServiceImpl;
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
public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID userId;
    private User ownerUser;
    private Project project;
    private ProjectMember ownerMember;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ownerUser = User.builder()
                .id(userId)
                .email("owner@example.com")
                .fullName("Owner User")
                .build();
        
        project = Project.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .description("Test Description")
                .ownerId(userId)
                .build();

        ownerMember = ProjectMember.builder()
                .id(UUID.randomUUID())
                .project(project)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();
    }

    @Test
    void createProject_Success_WithoutInvitations() {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("New Desc")
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(ownerMember);
        when(todoRepository.countByProjectId(any(UUID.class))).thenReturn(0L);

        ProjectResponse response = projectService.createProject(request, userId);

        assertNotNull(response);
        assertEquals("pending", response.getStatus());
        assertEquals(ProjectRole.OWNER, response.getRole());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
    }

    @Test
    void createProject_Success_WithInvitations() {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("New Desc")
                .memberEmails(Arrays.asList("invitee@example.com", ""))
                .build();

        User invitee = User.builder()
                .id(UUID.randomUUID())
                .email("invitee@example.com")
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(ownerMember);
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.existsByProjectIdAndUserId(any(), any())).thenReturn(false);
        when(todoRepository.countByProjectId(any(UUID.class))).thenReturn(0L);

        ProjectResponse response = projectService.createProject(request, userId);

        assertNotNull(response);
        verify(userRepository, times(1)).findByEmail("invitee@example.com");
        verify(projectMemberRepository, times(2)).save(any(ProjectMember.class));
    }

    @Test
    void createProject_InviteUserNotFound_ThrowsException() {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .memberEmails(Collections.singletonList("notfound@example.com"))
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(request, userId));
    }

    @Test
    void updateProject_Success() {
        ProjectRequest request = ProjectRequest.builder()
                .title("Updated Title")
                .description("Updated Desc")
                .memberEmails(Collections.singletonList("invitee@example.com"))
                .build();

        User invitee = User.builder()
                .id(UUID.randomUUID())
                .email("invitee@example.com")
                .build();

        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectId(project.getId())).thenReturn(new ArrayList<>(Collections.singletonList(ownerMember)));
        when(todoRepository.countByProjectId(any())).thenReturn(5L);
        when(todoRepository.countByProjectIdAndStatusNot(any(), eq(TodoStatus.COMPLETED))).thenReturn(0L);

        ProjectResponse response = projectService.updateProject(project.getId(), request, userId);

        assertNotNull(response);
        assertEquals("completed", response.getStatus());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_AccessDenied_ThrowsException() {
        ProjectRequest request = ProjectRequest.builder().title("Updated").build();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(IllegalArgumentException.class, () -> 
                projectService.updateProject(project.getId(), request, UUID.randomUUID())
        );
    }

    @Test
    void updateProject_NotFound_ThrowsException() {
        ProjectRequest request = ProjectRequest.builder().title("Updated").build();
        when(projectRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
                projectService.updateProject(UUID.randomUUID(), request, userId)
        );
    }

    @Test
    void getAllProjects_Success() {
        when(projectRepository.findAllByOwnerOrMember(userId)).thenReturn(Collections.singletonList(project));
        when(todoRepository.countByProjectId(any())).thenReturn(5L);
        when(todoRepository.countByProjectIdAndStatusNot(any(), eq(TodoStatus.COMPLETED))).thenReturn(2L);

        List<ProjectResponse> responses = projectService.getAllProjects(userId);

        assertEquals(1, responses.size());
        assertEquals("in_progress", responses.get(0).getStatus());
    }

    @Test
    void getProjectById_AsOwner_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(todoRepository.countByProjectId(any())).thenReturn(0L);

        ProjectResponse response = projectService.getProjectById(project.getId(), userId);

        assertNotNull(response);
        assertEquals(ProjectRole.OWNER, response.getRole());
    }

    @Test
    void getProjectById_AsMember_Success() {
        UUID memberId = UUID.randomUUID();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(todoRepository.countByProjectId(any())).thenReturn(0L);

        ProjectResponse response = projectService.getProjectById(project.getId(), memberId);

        assertNotNull(response);
        assertEquals(ProjectRole.MEMBER, response.getRole());
    }

    @Test
    void getProjectById_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                projectService.getProjectById(project.getId(), nonMemberId)
        );
    }

    @Test
    void getCategoriesByProject_Success() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .name("Sprint 1")
                .build();
        
        Todo uncategorizedTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(null)
                .title("Uncategorized task")
                .status(TodoStatus.PENDING)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(categoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(category));
        when(todoRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(uncategorizedTodo));

        List<DataByProjectResponse> responses = projectService.getCategoriesByProject(project.getId(), userId);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Sprint 1", responses.get(0).getName());
        assertEquals("Uncategorized task", responses.get(1).getTitle());
    }

    @Test
    void getCategoriesByProject_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                projectService.getCategoriesByProject(project.getId(), nonMemberId)
        );
    }

    @Test
    void deleteProject_Success_AsOwner() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .build();

        Todo categoryTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(category)
                .status(TodoStatus.COMPLETED) // Owner can delete completed
                .build();

        Todo projectTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(null)
                .status(TodoStatus.COMPLETED) // Owner can delete completed
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(categoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(categoryTodo));
        when(todoRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(projectTodo));

        projectService.deleteProject(project.getId(), userId);

        verify(todoRepository, times(1)).delete(categoryTodo);
        verify(categoryRepository, times(1)).delete(category);
        verify(todoRepository, times(1)).delete(projectTodo);
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    void deleteProject_Success_AsMember_NoCompletedTodos() {
        UUID memberId = UUID.randomUUID();
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .build();

        Todo categoryTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(category)
                .status(TodoStatus.PENDING)
                .build();

        Todo projectTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(null)
                .status(TodoStatus.PENDING)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(categoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(categoryTodo));
        when(todoRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(projectTodo));

        projectService.deleteProject(project.getId(), memberId);

        verify(todoRepository, times(1)).delete(categoryTodo);
        verify(categoryRepository, times(1)).delete(category);
        verify(todoRepository, times(1)).delete(projectTodo);
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    void deleteProject_Failure_AsMember_WithCompletedTodoInCategory() {
        UUID memberId = UUID.randomUUID();
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .build();

        Todo categoryTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(category)
                .status(TodoStatus.COMPLETED) // Member cannot delete if completed
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(categoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(categoryTodo));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.deleteProject(project.getId(), memberId)
        );
    }

    @Test
    void deleteProject_Failure_AsMember_WithCompletedTodoInProject() {
        UUID memberId = UUID.randomUUID();
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .project(project)
                .build();

        Todo categoryTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(category)
                .status(TodoStatus.PENDING)
                .build();

        Todo projectTodo = Todo.builder()
                .id(UUID.randomUUID())
                .project(project)
                .category(null)
                .status(TodoStatus.COMPLETED) // Member cannot delete if completed
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), memberId)).thenReturn(true);
        when(categoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(category));
        when(todoRepository.findByCategoryId(category.getId())).thenReturn(Collections.singletonList(categoryTodo));
        when(todoRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(projectTodo));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.deleteProject(project.getId(), memberId)
        );
    }

    @Test
    void deleteProject_AccessDenied_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), nonMemberId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                projectService.deleteProject(project.getId(), nonMemberId)
        );
    }
}
