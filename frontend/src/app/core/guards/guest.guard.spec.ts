import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { guestGuard } from './guest.guard';
import { AuthService } from '../services/auth.service';
import { provideEnvironment } from '../services/environment.service';
import { authInterceptor } from '../interceptors/auth.interceptor';

describe('guestGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', [], {
      isAuthenticated: false
    });
    Object.defineProperty(authService, 'isAuthenticated', {
      get: () => false,
      configurable: true
    });
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideAnimations(),
        provideEnvironment()
      ]
    });
  });

  it('should allow access when not authenticated', () => {
    Object.defineProperty(authService, 'isAuthenticated', {
      get: () => false,
      configurable: true
    });

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as any, { url: '/login' } as any)
    );

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to dashboard when authenticated', () => {
    Object.defineProperty(authService, 'isAuthenticated', {
      get: () => true,
      configurable: true
    });

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as any, { url: '/login' } as any)
    );

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});