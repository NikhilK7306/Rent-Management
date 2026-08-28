import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { ENVIRONMENT } from '../services/environment.token';
import { LoginRequest, LoginResponse, AuthState, UserDto } from '../models/auth.model';

const AUTH_STORAGE_KEY = 'rentms_auth_state';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private authStateSubject = new BehaviorSubject<AuthState>(this.getInitialState());
  authState$ = this.authStateSubject.asObservable();

  private getInitialState(): AuthState {
    if (!this.isBrowser) {
      return { user: null, accessToken: null, isAuthenticated: false };
    }
    try {
      const stored = localStorage.getItem(AUTH_STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored) as AuthState;
        if (parsed.accessToken && parsed.user) {
          return parsed;
        }
      }
    } catch {
      // Ignore parse errors
    }
    return { user: null, accessToken: null, isAuthenticated: false };
  }

  get currentAuthState(): AuthState {
    return this.authStateSubject.value;
  }

  get isAuthenticated(): boolean {
    return this.authStateSubject.value.isAuthenticated;
  }

  get currentUser(): UserDto | null {
    return this.authStateSubject.value.user;
  }

  get accessToken(): string | null {
    return this.authStateSubject.value.accessToken;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.env.apiBaseUrl}/auth/login`, credentials).pipe(
      tap(response => this.setAuthState(response)),
      catchError(error => {
        this.clearAuthState();
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.clearAuthState();
  }

  private setAuthState(response: LoginResponse): void {
    const state: AuthState = {
      user: response.user,
      accessToken: response.accessToken,
      isAuthenticated: true
    };
    this.authStateSubject.next(state);
    if (this.isBrowser) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state));
    }
  }

  private clearAuthState(): void {
    const state: AuthState = { user: null, accessToken: null, isAuthenticated: false };
    this.authStateSubject.next(state);
    if (this.isBrowser) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }
}