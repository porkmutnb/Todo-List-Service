import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { DialogService } from '../services/dialog.service';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.html',
})
export class MainLayoutComponent {
  readonly authService = inject(AuthService);
  readonly themeService = inject(ThemeService);
  private readonly dialogService = inject(DialogService);

  readonly mobileMenuOpen = signal(false);

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update((v) => !v);
  }

  async logout(): Promise<void> {
    const confirmed = await this.dialogService.confirm(
      'Sign Out',
      'Are you sure you want to sign out of your account?',
      'Yes, Sign Out',
      'Cancel'
    );
    if (confirmed) {
      this.authService.logout();
    }
  }
}
