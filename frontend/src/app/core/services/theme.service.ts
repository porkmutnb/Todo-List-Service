import { Injectable, signal, effect } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly THEME_KEY = 'todo_theme';
  
  // Create a signal representing the current active theme
  readonly currentTheme = signal<ThemeMode>('light');

  constructor() {
    // Load initial theme on service construction
    const savedTheme = localStorage.getItem(this.THEME_KEY) as ThemeMode | null;
    if (savedTheme === 'light' || savedTheme === 'dark') {
      this.currentTheme.set(savedTheme);
    } else {
      // Fallback to system preferences
      const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.currentTheme.set(systemPrefersDark ? 'dark' : 'light');
    }

    // Reactively update class and localStorage whenever currentTheme changes
    effect(() => {
      const theme = this.currentTheme();
      localStorage.setItem(this.THEME_KEY, theme);
      
      const root = document.documentElement;
      if (theme === 'dark') {
        root.classList.add('dark');
      } else {
        root.classList.remove('dark');
      }
    });
  }

  toggleTheme(): void {
    this.currentTheme.update((prev) => (prev === 'light' ? 'dark' : 'light'));
  }

  isDark(): boolean {
    return this.currentTheme() === 'dark';
  }
}
