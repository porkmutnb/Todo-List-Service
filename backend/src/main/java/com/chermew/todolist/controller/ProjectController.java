package com.chermew.todolist.controller;

import com.chermew.todolist.annotation.LogActivity;
import com.chermew.todolist.dto.*;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @LogActivity(action = "PROJECT_CREATE", entityType = "projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ProjectResponse response = projectService.createProject(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", response));
    }

    @PutMapping("/{projectId}")
    @LogActivity(action = "PROJECT_UPDATE", entityType = "projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ProjectResponse response = projectService.updateProject(projectId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    @GetMapping
    @LogActivity(action = "PROJECT_GET_ALL", entityType = "projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<ProjectResponse> response = projectService.getAllProjects(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully", response));
    }

    @GetMapping("/{projectId}")
    @LogActivity(action = "PROJECT_GET_BY_ID", entityType = "projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ProjectResponse response = projectService.getProjectById(projectId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully", response));
    }

    @GetMapping("/{projectId}/categories")
    @LogActivity(action = "CATEGORY_GET_BY_PROJECT", entityType = "categories")
    public ResponseEntity<ApiResponse<List<DataByProjectResponse>>> getCategoriesByProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<DataByProjectResponse> response = projectService.getCategoriesByProject(projectId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project By Categories fetched successfully", response));
    }

    @DeleteMapping("/{projectId}")
    @LogActivity(action = "PROJECT_DELETE", entityType = "projects")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        projectService.deleteProject(projectId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

}
