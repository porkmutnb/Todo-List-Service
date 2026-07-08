import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly SESSION_KEY = 'todo_user_session';

  // Signals
  readonly currentUser = signal<AuthResponse | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  constructor() {
    this.loadSession();
  }

  login(credentials: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>('/api/v1/login', credentials).pipe(
      tap((response) => {
        if (response.success && response.data) {
          this.saveSession(response.data);
        }
      })
    );
  }

  register(data: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>('/api/v1/register', data).pipe(
      tap((response) => {
        if (response.success && response.data) {
          this.saveSession(response.data);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.SESSION_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const session = this.currentUser();
    return session ? session.accessToken : null;
  }

  updateSessionName(fullName: string): void {
    const session = this.currentUser();
    if (session) {
      const updated = { ...session, fullName };
      this.saveSession(updated);
    }
  }

  updateSessionDetails(fullName: string, avatarUrl?: string): void {
    const session = this.currentUser();
    if (session) {
      const updated = { ...session, fullName, avatarUrl };
      this.saveSession(updated);
    }
  }

  private saveSession(auth: AuthResponse): void {
    localStorage.setItem(this.SESSION_KEY, JSON.stringify(auth));
    this.currentUser.set(auth);
  }

  private loadSession(): void {
    const saved = localStorage.getItem(this.SESSION_KEY);
    if (saved) {
      try {
        const authData: AuthResponse = JSON.parse(saved);
        this.currentUser.set(authData);
      } catch (e) {
        localStorage.removeItem(this.SESSION_KEY);
      }
    }
  }
}
