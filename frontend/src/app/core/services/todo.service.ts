import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/user.model';
import { TodoRequest, TodoResponse } from '../models/todo.model';

@Injectable({
  providedIn: 'root',
})
export class TodoService {
  private readonly http = inject(HttpClient);

  getTodoById(todoId: string): Observable<ApiResponse<TodoResponse>> {
    return this.http.get<ApiResponse<TodoResponse>>(`/api/v1/todos/${todoId}`);
  }

  createTodo(request: TodoRequest): Observable<ApiResponse<TodoResponse>> {
    return this.http.post<ApiResponse<TodoResponse>>('/api/v1/todos', request);
  }

  updateTodo(todoId: string, request: TodoRequest): Observable<ApiResponse<TodoResponse>> {
    return this.http.put<ApiResponse<TodoResponse>>(`/api/v1/todos/${todoId}`, request);
  }

  deleteTodo(todoId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`/api/v1/todos/${todoId}`);
  }

  getTodosByCategory(categoryId: string): Observable<ApiResponse<TodoResponse[]>> {
    return this.http.get<ApiResponse<TodoResponse[]>>(`/api/v1/todos/${categoryId}/todos`);
  }
}
