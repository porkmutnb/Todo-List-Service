package com.chermew.todolist.service.impl;

import com.chermew.todolist.dto.DataByProjectResponse;
import com.chermew.todolist.dto.ProjectRequest;
import com.chermew.todolist.dto.ProjectResponse;
import com.chermew.todolist.entity.*;
import com.chermew.todolist.enums.ProjectRole;
import com.chermew.todolist.enums.TodoStatus;
import com.chermew.todolist.repository.*;
import com.chermew.todolist.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, UUID userId) {
        // 1. Create and save project
        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .ownerId(userId)
                .build();
        Project savedProject = projectRepository.save(project);

        // 2. Add owner to project_members table
        ProjectMember member = ProjectMember.builder()
                .project(savedProject)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();
        projectMemberRepository.save(member);

        // 3. Process member invitations if provided during creation
        if (request.getMemberEmails() != null && !request.getMemberEmails().isEmpty()) {
            for (String email : request.getMemberEmails()) {
                if (email == null || email.trim().isEmpty()) continue;
                
                User invitee = userRepository.findByEmail(email.trim())
                        .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found"));
                
                // If not already a member (should not be since it is new, but let's check for safety)
                boolean alreadyMember = projectMemberRepository.existsByProjectIdAndUserId(savedProject.getId(), invitee.getId());
                if (!alreadyMember) {
                    ProjectMember newMember = ProjectMember.builder()
                            .project(savedProject)
                            .userId(invitee.getId())
                            .role(ProjectRole.MEMBER)
                            .build();
                    projectMemberRepository.save(newMember);
                }
            }
        }

        return mapToResponse(savedProject, ProjectRole.OWNER);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Security check: Only owner can edit
        if (!project.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Only the project owner can edit this project");
        }

        // 1. Update project basic details
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        Project updatedProject = projectRepository.save(project);

        // 2. Process member invitations & synchronization
        List<User> targetUsers = new ArrayList<>();
        if (request.getMemberEmails() != null) {
            for (String email : request.getMemberEmails()) {
                if (email == null || email.trim().isEmpty()) continue;
                
                User invitee = userRepository.findByEmail(email.trim())
                        .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found"));
                targetUsers.add(invitee);
            }
        }

        List<UUID> targetUserIds = targetUsers.stream().map(User::getId).collect(Collectors.toList());
        List<ProjectMember> currentMembers = projectMemberRepository.findByProjectId(projectId);

        // Remove members not present in request (excluding the project owner)
        for (ProjectMember member : currentMembers) {
            if (member.getRole() == ProjectRole.OWNER) {
                continue;
            }
            if (!targetUserIds.contains(member.getUserId())) {
                projectMemberRepository.delete(member);
            }
        }

        // Add new members not already in project
        for (User user : targetUsers) {
            boolean alreadyMember = currentMembers.stream()
                    .anyMatch(m -> m.getUserId().equals(user.getId()));
            if (!alreadyMember) {
                ProjectMember newMember = ProjectMember.builder()
                        .project(updatedProject)
                        .userId(user.getId())
                        .role(ProjectRole.MEMBER)
                        .build();
                projectMemberRepository.save(newMember);
            }
        }

        return mapToResponse(updatedProject, ProjectRole.OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(UUID userId) {
        List<Project> projects = projectRepository.findAllByOwnerOrMember(userId);

        return projects.stream()
                .map(project -> {
                    ProjectRole role = project.getOwnerId().equals(userId) ? ProjectRole.OWNER : ProjectRole.MEMBER;
                    return mapToResponse(project, role);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        ProjectRole role;
        if (project.getOwnerId().equals(userId)) {
            role = ProjectRole.OWNER;
        } else {
            // Check if user is a member
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
            if (!isMember) {
                throw new IllegalArgumentException("Access denied: You are not authorized to view this project");
            }
            role = ProjectRole.MEMBER;
        }

        return mapToResponse(project, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataByProjectResponse> getCategoriesByProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this project's categories");
        }

        List<DataByProjectResponse> dataByProjectResponseList = new ArrayList<>();
        dataByProjectResponseList.addAll(categoryRepository.findByProjectId(projectId).stream().map(this::mapToDataByProjectResponse).toList());
        dataByProjectResponseList.addAll(todoRepository.findByProjectId(projectId).stream().filter( todo -> todo.getCategory() == null).map(this::mapToDataByProjectResponse).toList());
        return dataByProjectResponseList;
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Security check: Owner or Member
        boolean isOwner = project.getOwnerId().equals(userId);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Access denied: You are not authorized to view this project's categories");
        }

        categoryRepository.findByProjectId(projectId).forEach(category -> {
            todoRepository.findByCategoryId(category.getId()).forEach(todo -> {
                if(isMember && TodoStatus.COMPLETED.equals(todo.getStatus())) {
                    throw new IllegalArgumentException("This todo for this category is completed and cannot be deleted");
                }
                todoRepository.delete(todo);
            });
            categoryRepository.delete(category);
        });

        todoRepository.findByProjectId(projectId).stream().filter( todo -> todo.getCategory() == null).forEach(todo -> {
            if(isMember && TodoStatus.COMPLETED.equals(todo.getStatus())) {
                throw new IllegalArgumentException("This todo is completed and cannot be deleted");
            }
            todoRepository.delete(todo);
        });

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project, ProjectRole role) {
        long totalTodos = todoRepository.countByProjectId(project.getId());
        String status;
        if (totalTodos == 0) {
            status = "pending";
        } else {
            long activeTodos = todoRepository.countByProjectIdAndStatusNot(project.getId(), TodoStatus.COMPLETED);
            status = (activeTodos == 0) ? "completed" : "in_progress";
        }
        List<UUID> memberList = projectMemberRepository.findByProjectId(project.getId()).stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .role(role)
                .status(status)
                .memberList(memberList)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private DataByProjectResponse mapToDataByProjectResponse(Category category) {
        return DataByProjectResponse.builder()
                .id(category.getId())
                .projectId(category.getProject().getId())
                .name(category.getName())
                .description(category.getDescription())
                .type("CATEGORY")
                .colorCode(category.getColorCode())
                .assignedTo(category.getAssignedTo())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private DataByProjectResponse mapToDataByProjectResponse(Todo todo) {
        return DataByProjectResponse.builder()
                .id(todo.getId())
                .categoryId(todo.getCategory() != null ? todo.getCategory().getId() : null)
                .projectId(todo.getProject().getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .type("TODO")
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
