import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideAnimations } from '@angular/platform-browser/animations';

import { AuthService } from './auth.service';
import { provideEnvironment } from './environment.service';
import { authInterceptor } from '../interceptors/auth.interceptor';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { ENVIRONMENT } from './environment.token';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const mockEnv = { production: false, apiBaseUrl: 'http://localhost:8080/api' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: ENVIRONMENT, useValue: mockEnv },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideAnimations()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have initial state as unauthenticated', () => {
    expect(service.isAuthenticated).toBeFalse();
    expect(service.currentUser).toBeNull();
    expect(service.accessToken).toBeNull();
  });

  it('should login and set auth state', () => {
    const credentials: LoginRequest = { mobileNumber: '9876543210', password: 'Admin@123' };
    const mockResponse: LoginResponse = {
      accessToken: 'test-jwt-token',
      tokenType: 'Bearer',
      user: { id: 1, name: 'System Admin', mobileNumber: '9876543210', email: 'admin@rentms.local', role: 'ADMIN' }
    };

    service.login(credentials).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);
    req.flush(mockResponse);

    expect(service.isAuthenticated).toBeTrue();
    expect(service.currentUser).toEqual(mockResponse.user);
    expect(service.accessToken).toBe('test-jwt-token');

    const stored = localStorage.getItem('rentms_auth_state');
    expect(stored).toBeTruthy();
    const parsed = JSON.parse(stored!);
    expect(parsed.isAuthenticated).toBeTrue();
    expect(parsed.accessToken).toBe('test-jwt-token');
  });

  it('should clear auth state on logout', () => {
    const mockResponse: LoginResponse = {
      accessToken: 'test-jwt-token',
      tokenType: 'Bearer',
      user: { id: 1, name: 'System Admin', mobileNumber: '9876543210', email: 'admin@rentms.local', role: 'ADMIN' }
    };

    service.login({ mobileNumber: '9876543210', password: 'Admin@123' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush(mockResponse);

    service.logout();

    expect(service.isAuthenticated).toBeFalse();
    expect(service.currentUser).toBeNull();
    expect(service.accessToken).toBeNull();
    expect(localStorage.getItem('rentms_auth_state')).toBeNull();
  });

  it('should clear auth state on login error', () => {
    const credentials: LoginRequest = { mobileNumber: '9876543210', password: 'WrongPassword' };

    service.login(credentials).subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.isAuthenticated).toBeFalse();
    expect(localStorage.getItem('rentms_auth_state')).toBeNull();
  });
});