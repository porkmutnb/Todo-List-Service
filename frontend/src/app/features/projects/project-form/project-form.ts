import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { forkJoin, of, Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ProjectService } from '../../../core/services/project.service';
import { UserService } from '../../../core/services/user.service';
import { CategoryService } from '../../../core/services/category.service';
import { TodoService } from '../../../core/services/todo.service';
import { DialogService } from '../../../shared/services/dialog.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { ProjectRequest } from '../../../core/models/project.model';

@Component({
  selector: 'app-project-form',
  imports: [ReactiveFormsModule, RouterLink, BreadcrumbComponent, DatePipe],
  templateUrl: './project-form.html',
})
export class ProjectFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly dialogService = inject(DialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly categoryService = inject(CategoryService);
  private readonly todoService = inject(TodoService);

  // States
  readonly mode = signal<'create' | 'view' | 'edit'>('create');
  readonly projectId = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly memberEmails = signal<string[]>([]);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);

  // Category and Todo states
  readonly projectItems = signal<any[]>([]);
  readonly isItemsLoading = signal(false);
  readonly userCache = signal<{ [userId: string]: string }>({});

  projectForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.determineModeAndLoad();
  }

  private initForm(): void {
    this.projectForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(150)]],
      description: ['', []],
      tempEmail: ['', [Validators.email]]
    });
  }

  private determineModeAndLoad(): void {
    const id = this.route.snapshot.paramMap.get('projectId');
    const url = this.router.url;

    if (!id) {
      this.mode.set('create');
      this.breadcrumbs.set([
        { label: 'Projects', url: '/projects' },
        { label: 'Create Project' }
      ]);
    } else {
      this.projectId.set(id);
      if (url.endsWith('/edit')) {
        this.mode.set('edit');
      } else {
        this.mode.set('view');
        this.loadProjectItems(id);
      }
      this.loadProject(id);
    }
  }

  private loadProject(id: string): void {
    this.isLoading.set(true);
    this.projectService.getProjectById(id).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          const p = response.data;
          this.projectForm.patchValue({
            title: p.title,
            description: p.description || ''
          });

          // Resolve member UUIDs to emails
          this.resolveMemberEmails(p.memberList || []).subscribe({
            next: (emails) => {
              this.isLoading.set(false);
              this.memberEmails.set(emails);
            },
            error: () => {
              this.isLoading.set(false);
            }
          });

          // Set breadcrumbs dynamically
          const baseBrs: BreadcrumbItem[] = [
            { label: 'Projects', url: '/projects' },
            { label: p.title, url: `/projects/${p.id}` }
          ];

          if (this.mode() === 'edit') {
            baseBrs.push({ label: 'Edit' });
            this.breadcrumbs.set(baseBrs);
          } else {
            this.breadcrumbs.set(baseBrs);
            // Disable all fields reactively for view mode
            this.projectForm.disable();
          }
        } else {
          this.isLoading.set(false);
          this.dialogService.error('Error', response.message || 'Failed to load project details.');
          this.router.navigate(['/projects']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || 'Error occurred while loading project.';
        this.dialogService.error('Retrieval Error', errMsg);
        this.router.navigate(['/projects']);
      }
    });
  }

  addMemberEmail(): void {
    const emailControl = this.projectForm.get('tempEmail');
    if (emailControl && emailControl.valid && emailControl.value) {
      const email = emailControl.value.trim().toLowerCase();
      if (!this.memberEmails().includes(email)) {
        this.memberEmails.update((prev) => [...prev, email]);
      }
      emailControl.reset();
    } else {
      emailControl?.markAsTouched();
    }
  }

  removeMemberEmail(index: number): void {
    if (this.mode() === 'view') return;
    this.memberEmails.update((prev) => prev.filter((_, i) => i !== index));
  }

  switchToEdit(): void {
    const id = this.projectId();
    if (id) {
      this.router.navigate([`/projects/${id}/edit`]);
    }
  }

  onSubmit(): void {
    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    const formVal = this.projectForm.value;

    const payload: ProjectRequest = {
      title: formVal.title,
      description: formVal.description || '',
      memberEmails: this.memberEmails()
    };

    const currentMode = this.mode();
    if (currentMode === 'create') {
      this.projectService.createProject(payload).subscribe({
        next: async (response) => {
          this.isSaving.set(false);
          if (response.success) {
            await this.dialogService.success('Success', 'Project created successfully.');
            this.router.navigate(['/projects']);
          } else {
            this.dialogService.error('Error', response.message || 'Failed to create project.');
          }
        },
        error: (err) => {
          this.isSaving.set(false);
          const errMsg = err.error?.message || 'Unable to create project.';
          this.dialogService.error('Creation Error', errMsg);
        }
      });
    } else if (currentMode === 'edit') {
      const id = this.projectId();
      if (id) {
        this.projectService.updateProject(id, payload).subscribe({
          next: async (response) => {
            this.isSaving.set(false);
            if (response.success) {
              await this.dialogService.success('Success', 'Project updated successfully.');
              this.router.navigate([`/projects/${id}`]);
            } else {
              this.dialogService.error('Error', response.message || 'Failed to update project.');
            }
          },
          error: (err) => {
            this.isSaving.set(false);
            const errMsg = err.error?.message || 'Unable to update project.';
            this.dialogService.error('Update Error', errMsg);
          }
        });
      }
    }
  }

  // Load Categories and Todos in view mode
  loadProjectItems(projectId: string): void {
    this.isItemsLoading.set(true);
    this.projectService.getCategoriesByProject(projectId).subscribe({
      next: (response: any) => {
        this.isItemsLoading.set(false);
        if (response.success && response.data) {
          this.projectItems.set(response.data);
          // Fetch assignee names reactively
          response.data.forEach((item: any) => {
            if (item.assignedTo) {
              this.fetchAssigneeName(item.assignedTo);
            }
          });
        }
      },
      error: () => {
        this.isItemsLoading.set(false);
      }
    });
  }

  fetchAssigneeName(userId: string): void {
    if (this.userCache()[userId]) return;

    // Set placeholder to prevent duplicate HTTP calls
    this.userCache.update(cache => ({ ...cache, [userId]: 'Loading...' }));

    this.userService.getUserById(userId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.userCache.update(cache => ({ ...cache, [userId]: response.data.fullName }));
        } else {
          this.userCache.update(cache => ({ ...cache, [userId]: 'Unknown User' }));
        }
      },
      error: () => {
        this.userCache.update(cache => ({ ...cache, [userId]: 'Unknown User' }));
      }
    });
  }

  deleteCategory(categoryId: string, name: string): void {
    this.dialogService.confirm(
      'Delete Category',
      `Are you sure you want to delete category "${name}"? This will also delete any tasks within this category.`,
      'Yes, Delete',
      'Cancel'
    ).then((confirmed) => {
      if (confirmed) {
        this.categoryService.deleteCategory(categoryId).subscribe({
          next: (response) => {
            if (response.success) {
              this.dialogService.success('Success', 'Category deleted successfully.');
              const id = this.projectId();
              if (id) this.loadProjectItems(id);
            } else {
              this.dialogService.error('Error', response.message || 'Failed to delete category.');
            }
          },
          error: (err) => {
            const errMsg = err.error?.message || 'Error occurred while deleting category.';
            this.dialogService.error('Deletion Error', errMsg);
          }
        });
      }
    });
  }

  deleteTodo(todoId: string, title: string): void {
    this.dialogService.confirm(
      'Delete Todo',
      `Are you sure you want to delete task "${title}"?`,
      'Yes, Delete',
      'Cancel'
    ).then((confirmed) => {
      if (confirmed) {
        this.todoService.deleteTodo(todoId).subscribe({
          next: (response) => {
            if (response.success) {
              this.dialogService.success('Success', 'Task deleted successfully.');
              const id = this.projectId();
              if (id) this.loadProjectItems(id);
            } else {
              this.dialogService.error('Error', response.message || 'Failed to delete task.');
            }
          },
          error: (err) => {
            const errMsg = err.error?.message || 'Error occurred while deleting task.';
            this.dialogService.error('Deletion Error', errMsg);
          }
        });
      }
    });
  }

  resolveMemberEmails(memberList: string[]): Observable<string[]> {
    if (!memberList || memberList.length === 0) {
      return of([]);
    }
    const requests = memberList.map(userId =>
      this.userService.getUserById(userId).pipe(
        map(resp => (resp.success && resp.data) ? resp.data.email : null),
        catchError(() => of(null))
      )
    );
    return forkJoin(requests).pipe(
      map(emails => emails.filter((email): email is string => !!email))
    );
  }

  get title() { return this.projectForm.get('title'); }
  get tempEmail() { return this.projectForm.get('tempEmail'); }
}
