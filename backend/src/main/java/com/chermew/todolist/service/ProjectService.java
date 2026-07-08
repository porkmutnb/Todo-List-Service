package com.chermew.todolist.service;

import com.chermew.todolist.dto.DataByProjectResponse;
import com.chermew.todolist.dto.ProjectRequest;
import com.chermew.todolist.dto.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request, UUID userId);
    ProjectResponse updateProject(UUID projectId, ProjectRequest request, UUID userId);
    List<ProjectResponse> getAllProjects(UUID userId);
    ProjectResponse getProjectById(UUID projectId, UUID userId);
    List<DataByProjectResponse> getCategoriesByProject(UUID projectId, UUID userId);
    void deleteProject(UUID projectId, UUID id);
}
