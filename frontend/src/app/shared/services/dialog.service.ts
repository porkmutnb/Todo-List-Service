import { Injectable, signal } from '@angular/core';

export interface DialogConfig {
  type: 'success' | 'error' | 'warning' | 'confirm';
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  resolve?: (value: boolean) => void;
}

@Injectable({
  providedIn: 'root',
})
export class DialogService {
  readonly activeDialog = signal<DialogConfig | null>(null);

  success(title: string, message: string, confirmText = 'OK'): Promise<boolean> {
    return this.show({ type: 'success', title, message, confirmText });
  }

  error(title: string, message: string, confirmText = 'Close'): Promise<boolean> {
    return this.show({ type: 'error', title, message, confirmText });
  }

  warning(title: string, message: string, confirmText = 'Proceed', cancelText = 'Cancel'): Promise<boolean> {
    return this.show({ type: 'warning', title, message, confirmText, cancelText });
  }

  confirm(title: string, message: string, confirmText = 'Confirm', cancelText = 'Cancel'): Promise<boolean> {
    return this.show({ type: 'confirm', title, message, confirmText, cancelText });
  }

  close(result: boolean): void {
    const current = this.activeDialog();
    if (current && current.resolve) {
      current.resolve(result);
    }
    this.activeDialog.set(null);
  }

  private show(config: Omit<DialogConfig, 'resolve'>): Promise<boolean> {
    // If there is an active dialog, close it first
    if (this.activeDialog()) {
      this.close(false);
    }

    return new Promise<boolean>((resolve) => {
      this.activeDialog.set({
        ...config,
        resolve,
      });
    });
  }
}
