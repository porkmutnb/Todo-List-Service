import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/user.model';
import { ProjectRequest, ProjectResponse } from '../models/project.model';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly http = inject(HttpClient);

  getAllProjects(): Observable<ApiResponse<ProjectResponse[]>> {
    return this.http.get<ApiResponse<ProjectResponse[]>>('/api/v1/projects');
  }

  getProjectById(projectId: string): Observable<ApiResponse<ProjectResponse>> {
    return this.http.get<ApiResponse<ProjectResponse>>(`/api/v1/projects/${projectId}`);
  }

  createProject(request: ProjectRequest): Observable<ApiResponse<ProjectResponse>> {
    return this.http.post<ApiResponse<ProjectResponse>>('/api/v1/projects', request);
  }

  updateProject(projectId: string, request: ProjectRequest): Observable<ApiResponse<ProjectResponse>> {
    return this.http.put<ApiResponse<ProjectResponse>>(`/api/v1/projects/${projectId}`, request);
  }

  deleteProject(projectId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`/api/v1/projects/${projectId}`);
  }

  getCategoriesByProject(projectId: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`/api/v1/projects/${projectId}/categories`);
  }
}
