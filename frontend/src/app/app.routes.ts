import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/layout/main-layout').then(m => m.MainLayoutComponent),
    children: [
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then(m => m.ProfileComponent),
      },
      {
        path: 'projects',
        loadComponent: () => import('./features/projects/project-list/project-list').then(m => m.ProjectListComponent),
      },
      {
        path: 'projects/new',
        loadComponent: () => import('./features/projects/project-form/project-form').then(m => m.ProjectFormComponent),
      },
      {
        path: 'projects/:projectId',
        loadComponent: () => import('./features/projects/project-form/project-form').then(m => m.ProjectFormComponent),
      },
      {
        path: 'projects/:projectId/edit',
        loadComponent: () => import('./features/projects/project-form/project-form').then(m => m.ProjectFormComponent),
      },
      // Category Routes
      {
        path: 'projects/:projectId/categories/new',
        loadComponent: () => import('./features/categories/category-form/category-form').then(m => m.CategoryFormComponent),
      },
      {
        path: 'projects/:projectId/categories/:categoryId',
        loadComponent: () => import('./features/categories/category-form/category-form').then(m => m.CategoryFormComponent),
      },
      {
        path: 'projects/:projectId/categories/:categoryId/edit',
        loadComponent: () => import('./features/categories/category-form/category-form').then(m => m.CategoryFormComponent),
      },
      // Todo Routes
      {
        path: 'projects/:projectId/todos/new',
        loadComponent: () => import('./features/todos/todo-form/todo-form').then(m => m.TodoFormComponent),
      },
      {
        path: 'projects/:projectId/todos/:todoId',
        loadComponent: () => import('./features/todos/todo-form/todo-form').then(m => m.TodoFormComponent),
      },
      {
        path: 'projects/:projectId/todos/:todoId/edit',
        loadComponent: () => import('./features/todos/todo-form/todo-form').then(m => m.TodoFormComponent),
      },
      {
        path: '',
        redirectTo: 'profile',
        pathMatch: 'full',
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login',
  }
];
