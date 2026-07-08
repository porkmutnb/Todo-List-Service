import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TodoService } from '../../../core/services/todo.service';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { DialogService } from '../../../shared/services/dialog.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { TodoRequest, TodoStatus, TodoPriority } from '../../../core/models/todo.model';

@Component({
  selector: 'app-todo-form',
  imports: [ReactiveFormsModule, RouterLink, BreadcrumbComponent],
  templateUrl: './todo-form.html',
})
export class TodoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly todoService = inject(TodoService);
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly dialogService = inject(DialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // States
  readonly mode = signal<'create' | 'view' | 'edit'>('create');
  readonly projectId = signal<string>('');
  readonly todoId = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);
  readonly projectTitle = signal<string>('');

  // Loaded Categories list for dropdown selection
  readonly projectCategories = signal<any[]>([]);

  todoForm!: FormGroup;

  // Options
  readonly statuses: { value: TodoStatus; label: string }[] = [
    { value: 'pending', label: 'Pending' },
    { value: 'in_progress', label: 'In Progress' },
    { value: 'completed', label: 'Completed' }
  ];

  readonly priorities: { value: TodoPriority; label: string }[] = [
    { value: 'low', label: 'Low' },
    { value: 'medium', label: 'Medium' },
    { value: 'high', label: 'High' }
  ];

  // Assignee selection helper: Current User is always a default option
  readonly assignableUsers = computed(() => {
    const current = this.authService.currentUser();
    return current ? [current] : [];
  });

  ngOnInit(): void {
    this.projectId.set(this.route.snapshot.paramMap.get('projectId') || '');
    this.initForm();
    this.loadProjectDetailsAndCategories();
  }

  private initForm(): void {
    this.todoForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', []],
      status: ['pending', [Validators.required]],
      priority: ['medium', [Validators.required]],
      dueDate: [null, []],
      assignedTo: [null, []],
      categoryId: [null, []]
    });
  }

  private loadProjectDetailsAndCategories(): void {
    const pId = this.projectId();
    const tId = this.route.snapshot.paramMap.get('todoId');
    const url = this.router.url;

    this.isLoading.set(true);

    // 1. Fetch project details
    this.projectService.getProjectById(pId).subscribe({
      next: (projResponse) => {
        if (projResponse.success && projResponse.data) {
          this.projectTitle.set(projResponse.data.title);

          // 2. Fetch categories for select dropdown
          this.projectService.getCategoriesByProject(pId).subscribe({
            next: (itemsResponse: any) => {
              if (itemsResponse.success && itemsResponse.data) {
                // Keep only Category items (using type key)
                const cats = itemsResponse.data.filter((item: any) => item.type?.toUpperCase() === 'CATEGORY');
                this.projectCategories.set(cats);
              }

              // 3. Determine mode and load details
              if (!tId) {
                this.mode.set('create');
                this.isLoading.set(false);
                this.breadcrumbs.set([
                  { label: 'Projects', url: '/projects' },
                  { label: projResponse.data.title, url: `/projects/${pId}` },
                  { label: 'New Task' }
                ]);
                
                // Pre-populate Category dropdown from query parameters
                const queryCatId = this.route.snapshot.queryParamMap.get('categoryId');
                if (queryCatId) {
                  this.todoForm.patchValue({ categoryId: queryCatId });
                }
              } else {
                this.todoId.set(tId);
                if (url.endsWith('/edit')) {
                  this.mode.set('edit');
                } else {
                  this.mode.set('view');
                }
                this.loadTodoDetails(tId, projResponse.data.title);
              }
            },
            error: () => this.fallbackLoad(projResponse.data.title)
          });

        } else {
          this.dialogService.error('Error', 'Unable to retrieve project details.');
          this.router.navigate(['/projects']);
        }
      },
      error: () => {
        this.dialogService.error('Error', 'Unable to load project workspace.');
        this.router.navigate(['/projects']);
      }
    });
  }

  private fallbackLoad(projTitle: string): void {
    this.isLoading.set(false);
    this.breadcrumbs.set([
      { label: 'Projects', url: '/projects' },
      { label: projTitle, url: `/projects/${this.projectId()}` },
      { label: 'Task Info' }
    ]);
  }

  private loadTodoDetails(tId: string, projTitle: string): void {
    this.todoService.getTodoById(tId).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response.success && response.data) {
          const todo = response.data;
          
          // Format ISO date format to YYYY-MM-DD for HTML5 date input
          let formattedDate: string | null = null;
          if (todo.dueDate) {
            formattedDate = todo.dueDate.substring(0, 10);
          }

          this.todoForm.patchValue({
            title: todo.title,
            description: todo.description || '',
            status: todo.status,
            priority: todo.priority,
            dueDate: formattedDate,
            assignedTo: todo.assignedTo || null,
            categoryId: todo.categoryId || null
          });

          // Set breadcrumbs dynamically
          const baseBrs: BreadcrumbItem[] = [
            { label: 'Projects', url: '/projects' },
            { label: projTitle, url: `/projects/${this.projectId()}` },
            { label: todo.title, url: `/projects/${this.projectId()}/todos/${todo.id}` }
          ];

          if (this.mode() === 'edit') {
            baseBrs.push({ label: 'Edit' });
            this.breadcrumbs.set(baseBrs);
          } else {
            this.breadcrumbs.set(baseBrs);
            this.todoForm.disable(); // Disables inputs reactively
          }
        } else {
          this.dialogService.error('Error', response.message || 'Failed to fetch task.');
          this.router.navigate([`/projects/${this.projectId()}`]);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || 'Error occurred while loading task.';
        this.dialogService.error('Task Load Error', errMsg);
        this.router.navigate([`/projects/${this.projectId()}`]);
      }
    });
  }

  switchToEdit(): void {
    const tId = this.todoId();
    if (tId) {
      this.router.navigate([`/projects/${this.projectId()}/todos/${tId}/edit`]);
    }
  }

  onSubmit(): void {
    if (this.todoForm.invalid) {
      this.todoForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    const formVal = this.todoForm.value;

    // Convert local date string to OffsetDateTime string format
    let dateStr: string | null = null;
    if (formVal.dueDate) {
      dateStr = new Date(formVal.dueDate).toISOString();
    }

    const payload: TodoRequest = {
      projectId: this.projectId(),
      categoryId: formVal.categoryId || null,
      title: formVal.title,
      description: formVal.description || '',
      status: formVal.status,
      priority: formVal.priority,
      dueDate: dateStr,
      assignedTo: formVal.assignedTo || null
    };

    const currentMode = this.mode();
    if (currentMode === 'create') {
      this.todoService.createTodo(payload).subscribe({
        next: async (response) => {
          this.isSaving.set(false);
          if (response.success) {
            await this.dialogService.success('Success', 'Task created successfully.');
            this.router.navigate([`/projects/${this.projectId()}`]);
          } else {
            this.dialogService.error('Error', response.message || 'Failed to create task.');
          }
        },
        error: (err) => {
          this.isSaving.set(false);
          const errMsg = err.error?.message || 'Unable to create task.';
          this.dialogService.error('Creation Error', errMsg);
        }
      });
    } else if (currentMode === 'edit') {
      const tId = this.todoId();
      if (tId) {
        this.todoService.updateTodo(tId, payload).subscribe({
          next: async (response) => {
            this.isSaving.set(false);
            if (response.success) {
              await this.dialogService.success('Success', 'Task updated successfully.');
              this.router.navigate([`/projects/${this.projectId()}/todos/${tId}`]);
            } else {
              this.dialogService.error('Error', response.message || 'Failed to update task.');
            }
          },
          error: (err) => {
            this.isSaving.set(false);
            const errMsg = err.error?.message || 'Unable to update task.';
            this.dialogService.error('Update Error', errMsg);
          }
        });
      }
    }
  }

  get title() { return this.todoForm.get('title'); }
}
