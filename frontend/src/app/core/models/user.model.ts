export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export type UserGender = 'male' | 'female';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | string;

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
  password?: string;
  newPassword?: string;
  bio?: UserGender | '';
  avatarUrl?: string;
}

export interface UserProfileResponse {
  id: string;
  email: string;
  fullName: string;
  status: UserStatus;
  bio: UserGender | '';
  avatarUrl: string;
  createdAt: string;
  updatedAt: string;
}
