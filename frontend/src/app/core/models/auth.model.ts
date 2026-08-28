export interface LoginRequest {
  mobileNumber: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: UserDto;
}

export interface UserDto {
  id: number;
  name: string;
  mobileNumber: string;
  email: string;
  role: 'ADMIN';
}

export interface AuthState {
  user: UserDto | null;
  accessToken: string | null;
  isAuthenticated: boolean;
}