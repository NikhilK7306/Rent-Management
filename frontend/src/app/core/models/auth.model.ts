export interface LoginRequest {
  mobileNumber: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: UserDto;
}

export type UserRole = 'ADMIN' | 'TENANT';

export interface UserDto {
  id: number;
  name: string;
  mobileNumber: string;
  email: string;
  role: UserRole;
}

export interface AuthState {
  user: UserDto | null;
  accessToken: string | null;
  isAuthenticated: boolean;
}