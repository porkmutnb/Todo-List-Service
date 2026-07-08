import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { DialogService } from '../../shared/services/dialog.service';
import { UserProfileResponse } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './profile.html',
})
export class ProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly dialogService = inject(DialogService);
  private readonly fb = inject(FormBuilder);

  readonly profile = signal<UserProfileResponse | null>(null);
  readonly isEditing = signal(false);
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly avatarBase64 = signal<string | null>(null);

  profileForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadProfile();
  }

  private initForm(): void {
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      bio: ['', []], // Stores UserGender ('male' | 'female' | '')
      password: ['', []], // Current password
      newPassword: ['', [Validators.minLength(6)]] // Optional new password
    });
  }

  loadProfile(): void {
    this.isLoading.set(true);
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response.success && response.data) {
          this.profile.set(response.data);
          this.populateForm(response.data);
        } else {
          this.dialogService.error('Error', response.message || 'Failed to load profile.');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || 'Unable to retrieve profile information.';
        this.dialogService.error('Profile Retrieval Error', errMsg);
      }
    });
  }

  private populateForm(p: UserProfileResponse): void {
    this.profileForm.patchValue({
      fullName: p.fullName,
      email: p.email,
      bio: p.bio || '',
      password: '',
      newPassword: ''
    });
  }

  toggleEdit(): void {
    const editState = this.isEditing();
    if (editState) {
      // Revert changes and repopulate form with current state
      const currentProfile = this.profile();
      if (currentProfile) {
        this.populateForm(currentProfile);
      }
      this.avatarBase64.set(null);
    }
    this.isEditing.set(!editState);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate image types
      const validTypes = ['image/jpeg', 'image/png'];
      if (!validTypes.includes(file.type)) {
        this.dialogService.error('Invalid File Type', 'Please select only JPG or PNG images.');
        input.value = ''; // Reset input element
        return;
      }

      const reader = new FileReader();
      reader.onload = (e: any) => {
        const img = new Image();
        img.onload = () => {
          // Resize the image using HTML5 Canvas
          const canvas = document.createElement('canvas');
          const maxDimension = 150;
          let width = img.width;
          let height = img.height;

          if (width > height) {
            if (width > maxDimension) {
              height = Math.round((height * maxDimension) / width);
              width = maxDimension;
            }
          } else {
            if (height > maxDimension) {
              width = Math.round((width * maxDimension) / height);
              height = maxDimension;
            }
          }

          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (ctx) {
            ctx.drawImage(img, 0, 0, width, height);
            // Export compressed data URL
            const base64 = canvas.toDataURL(file.type, 0.85);
            this.avatarBase64.set(base64);
          }
        };
        img.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    const formVal = this.profileForm.value;

    // Build update request, filtering empty fields
    const updatePayload: any = {
      fullName: formVal.fullName,
      email: formVal.email,
      bio: formVal.bio || null,
    };

    if (this.avatarBase64()) {
      updatePayload.avatarUrl = this.avatarBase64();
    }

    if (formVal.password) {
      updatePayload.password = formVal.password;
    }
    if (formVal.newPassword) {
      updatePayload.newPassword = formVal.newPassword;
    }

    this.userService.updateProfile(updatePayload).subscribe({
      next: (response) => {
        this.isSaving.set(false);
        if (response.success && response.data) {
          this.profile.set(response.data);
          this.populateForm(response.data);
          this.isEditing.set(false);
          this.avatarBase64.set(null); // Clear preview selection state

          // Keep navbar/sidebar session details synchronized in real-time
          this.authService.updateSessionDetails(response.data.fullName, response.data.avatarUrl);

          this.dialogService.success('Success', 'Profile updated successfully.');
        } else {
          this.dialogService.error('Update Failed', response.message || 'Failed to update profile.');
        }
      },
      error: (err) => {
        this.isSaving.set(false);
        const errMsg = err.error?.message || 'Error occurred while updating profile.';
        this.dialogService.error('Profile Update Error', errMsg);
      }
    });
  }

  get fullName() { return this.profileForm.get('fullName'); }
  get email() { return this.profileForm.get('email'); }
  get newPassword() { return this.profileForm.get('newPassword'); }
}
