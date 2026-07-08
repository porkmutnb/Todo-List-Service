import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CategoryService } from '../../../core/services/category.service';
import { ProjectService } from '../../../core/services/project.service';
import { TodoService } from '../../../core/services/todo.service';
import { UserService } from '../../../core/services/user.service';
import { DialogService } from '../../../shared/services/dialog.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { CategoryRequest } from '../../../core/models/category.model';
import { TodoResponse } from '../../../core/models/todo.model';

@Component({
  selector: 'app-category-form',
  imports: [ReactiveFormsModule, RouterLink, BreadcrumbComponent, DatePipe],
  templateUrl: './category-form.html',
})
export class CategoryFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);
  private readonly projectService = inject(ProjectService);
  private readonly dialogService = inject(DialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly todoService = inject(TodoService);
  private readonly userService = inject(UserService);

  // States
  readonly mode = signal<'create' | 'view' | 'edit'>('create');
  readonly projectId = signal<string>('');
  readonly categoryId = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);
  readonly projectTitle = signal<string>('');

  // Category specific todos
  readonly categoryTodos = signal<TodoResponse[]>([]);
  readonly isTodosLoading = signal(false);
  readonly userCache = signal<{ [userId: string]: string }>({});

  categoryForm!: FormGroup;

  // Preset color palette for categories
  readonly colorPalette = [
    { name: 'Indigo', value: '#4f46e5' },
    { name: 'Rose', value: '#e11d48' },
    { name: 'Emerald', value: '#10b981' },
    { name: 'Amber', value: '#d97706' },
    { name: 'Sky', value: '#0284c7' },
    { name: 'Violet', value: '#7c3aed' },
    { name: 'Teal', value: '#0d9488' }
  ];

  ngOnInit(): void {
    this.projectId.set(this.route.snapshot.paramMap.get('projectId') || '');
    this.initForm();
    this.loadProjectDetailsAndDetermineMode();
  }

  private initForm(): void {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', []],
      colorCode: ['#4f46e5', [Validators.required, Validators.maxLength(7)]]
    });
  }

  private loadProjectDetailsAndDetermineMode(): void {
    const pId = this.projectId();
    const cId = this.route.snapshot.paramMap.get('categoryId');
    const url = this.router.url;

    this.isLoading.set(true);
    // 1. Fetch project title for breadcrumbs first
    this.projectService.getProjectById(pId).subscribe({
      next: (projResponse) => {
        if (projResponse.success && projResponse.data) {
          this.projectTitle.set(projResponse.data.title);

          // 2. Determine mode
          if (!cId) {
            this.mode.set('create');
            this.isLoading.set(false);
            this.breadcrumbs.set([
              { label: 'Projects', url: '/projects' },
              { label: projResponse.data.title, url: `/projects/${pId}` },
              { label: 'New Category' }
            ]);
          } else {
            this.categoryId.set(cId);
            if (url.endsWith('/edit')) {
              this.mode.set('edit');
            } else {
              this.mode.set('view');
            }
            this.loadCategoryDetails(cId, projResponse.data.title);
          }
        } else {
          this.dialogService.error('Error', 'Unable to fetch project contexts.');
          this.router.navigate(['/projects']);
        }
      },
      error: () => {
        this.dialogService.error('Error', 'Unable to load project workspace.');
        this.router.navigate(['/projects']);
      }
    });
  }

  private loadCategoryDetails(cId: string, projTitle: string): void {
    this.categoryService.getCategoryById(cId).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response.success && response.data) {
          const cat = response.data;
          this.categoryForm.patchValue({
            name: cat.name,
            description: cat.description || '',
            colorCode: cat.colorCode || '#4f46e5'
          });

          // Set breadcrumbs dynamically
          const baseBrs: BreadcrumbItem[] = [
            { label: 'Projects', url: '/projects' },
            { label: projTitle, url: `/projects/${this.projectId()}` },
            { label: cat.name, url: `/projects/${this.projectId()}/categories/${cat.id}` }
          ];

          if (this.mode() === 'edit') {
            baseBrs.push({ label: 'Edit' });
            this.breadcrumbs.set(baseBrs);
          } else {
            this.breadcrumbs.set(baseBrs);
            this.categoryForm.disable(); // Disables fields for view mode
            this.loadCategoryTodos(cId);
          }
        } else {
          this.dialogService.error('Error', response.message || 'Failed to fetch category.');
          this.router.navigate([`/projects/${this.projectId()}`]);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || 'Error occurred while loading category.';
        this.dialogService.error('Category Load Error', errMsg);
        this.router.navigate([`/projects/${this.projectId()}`]);
      }
    });
  }

  selectColor(hex: string): void {
    if (this.mode() === 'view') return;
    this.categoryForm.patchValue({ colorCode: hex });
  }

  switchToEdit(): void {
    const cId = this.categoryId();
    if (cId) {
      this.router.navigate([`/projects/${this.projectId()}/categories/${cId}/edit`]);
    }
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    const formVal = this.categoryForm.value;

    const payload: CategoryRequest = {
      projectId: this.projectId(),
      name: formVal.name,
      description: formVal.description || '',
      colorCode: formVal.colorCode
    };

    const currentMode = this.mode();
    if (currentMode === 'create') {
      this.categoryService.createCategory(payload).subscribe({
        next: async (response) => {
          this.isSaving.set(false);
          if (response.success) {
            await this.dialogService.success('Success', 'Category created successfully.');
            this.router.navigate([`/projects/${this.projectId()}`]);
          } else {
            this.dialogService.error('Error', response.message || 'Failed to create category.');
          }
        },
        error: (err) => {
          this.isSaving.set(false);
          const errMsg = err.error?.message || 'Unable to create category.';
          this.dialogService.error('Creation Error', errMsg);
        }
      });
    } else if (currentMode === 'edit') {
      const cId = this.categoryId();
      if (cId) {
        this.categoryService.updateCategory(cId, payload).subscribe({
          next: async (response) => {
            this.isSaving.set(false);
            if (response.success) {
              await this.dialogService.success('Success', 'Category updated successfully.');
              this.router.navigate([`/projects/${this.projectId()}/categories/${cId}`]);
            } else {
              this.dialogService.error('Error', response.message || 'Failed to update category.');
            }
          },
          error: (err) => {
            this.isSaving.set(false);
            const errMsg = err.error?.message || 'Unable to update category.';
            this.dialogService.error('Update Error', errMsg);
          }
        });
      }
    }
  }

  loadCategoryTodos(cId: string): void {
    this.isTodosLoading.set(true);
    this.todoService.getTodosByCategory(cId).subscribe({
      next: (response: any) => {
        this.isTodosLoading.set(false);
        if (response.success && response.data) {
          this.categoryTodos.set(response.data);
          // Prefetch assignee names reactively
          response.data.forEach((item: any) => {
            if (item.assignedTo) {
              this.fetchAssigneeName(item.assignedTo);
            }
          });
        }
      },
      error: () => {
        this.isTodosLoading.set(false);
      }
    });
  }

  fetchAssigneeName(userId: string): void {
    if (this.userCache()[userId]) return;

    this.userCache.update(cache => ({ ...cache, [userId]: 'Loading...' }));

    this.userService.getUserById(userId).subscribe({
      next: (response: any) => {
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

  deleteTodo(todoId: string, title: string): void {
    this.dialogService.confirm(
      'Delete Todo',
      `Are you sure you want to delete task "${title}"?`,
      'Yes, Delete',
      'Cancel'
    ).then((confirmed) => {
      if (confirmed) {
        this.todoService.deleteTodo(todoId).subscribe({
          next: (response: any) => {
            if (response.success) {
              this.dialogService.success('Success', 'Task deleted successfully.');
              const cId = this.categoryId();
              if (cId) this.loadCategoryTodos(cId);
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

  get name() { return this.categoryForm.get('name'); }
  get colorCode() { return this.categoryForm.get('colorCode'); }
}
