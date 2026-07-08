import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ProjectService } from '../../../core/services/project.service';
import { DialogService } from '../../../shared/services/dialog.service';
import { DataTableComponent, ColumnConfig } from '../../../shared/components/data-table/data-table';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { ProjectResponse } from '../../../core/models/project.model';

@Component({
  selector: 'app-project-list',
  imports: [RouterLink, DataTableComponent, BreadcrumbComponent],
  templateUrl: './project-list.html',
})
export class ProjectListComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);

  // States
  readonly projects = signal<ProjectResponse[]>([]);
  readonly isLoading = signal(true);
  readonly breadcrumbs: BreadcrumbItem[] = [{ label: 'Projects' }];

  // Column definitions for reusable Datatable
  readonly columns: ColumnConfig<ProjectResponse>[] = [
    { key: 'title', header: 'Project Name', sortable: true, filterable: true },
    { key: 'description', header: 'Description', sortable: true, filterable: true },
    { key: 'role', header: 'My Role', sortable: true, filterable: true },
    { 
      key: 'createdAt', 
      header: 'Created Date', 
      sortable: true,
      render: (row) => new Date(row.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    }
  ];

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.isLoading.set(true);
    this.projectService.getAllProjects().subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response.success && response.data) {
          this.projects.set(response.data);
        } else {
          this.dialogService.error('Error', response.message || 'Failed to load projects.');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || 'Unable to retrieve projects.';
        this.dialogService.error('Projects Load Error', errMsg);
      }
    });
  }

  deleteProject(project: ProjectResponse): void {
    this.dialogService.confirm(
      'Delete Project',
      `Are you sure you want to delete the project "${project.title}"? All associated categories and tasks will be deleted permanentally.`,
      'Yes, Delete',
      'Cancel'
    ).then((confirmed) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.projectService.deleteProject(project.id).subscribe({
          next: (response) => {
            if (response.success) {
              this.dialogService.success('Success', 'Project deleted successfully.');
              this.loadProjects();
            } else {
              this.isLoading.set(false);
              this.dialogService.error('Error', response.message || 'Failed to delete project.');
            }
          },
          error: (err) => {
            this.isLoading.set(false);
            const errMsg = err.error?.message || 'Error occurred while deleting project.';
            this.dialogService.error('Deletion Error', errMsg);
          }
        });
      }
    });
  }
}
