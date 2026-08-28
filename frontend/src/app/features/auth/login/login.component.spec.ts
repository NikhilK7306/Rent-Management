import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '@core/services/auth.service';
import { provideEnvironment } from '@core/services/environment.service';
import { authInterceptor } from '@core/interceptors/auth.interceptor';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: any;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login'], {
      isAuthenticated: false
    });
    Object.defineProperty(authServiceSpy, 'isAuthenticated', {
      get: () => false,
      configurable: true
    });
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideAnimations(),
        provideEnvironment()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.loginForm.get('mobileNumber')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });

  it('should validate mobile number pattern', () => {
    const mobileControl = component.loginForm.get('mobileNumber');
    mobileControl?.setValue('123');
    expect(mobileControl?.invalid).toBeTrue();
    expect(mobileControl?.errors?.['pattern']).toBeTruthy();

    mobileControl?.setValue('9876543210');
    expect(mobileControl?.valid).toBeTrue();
  });

  it('should validate password min length', () => {
    const passwordControl = component.loginForm.get('password');
    passwordControl?.setValue('123');
    expect(passwordControl?.invalid).toBeTrue();
    expect(passwordControl?.errors?.['minlength']).toBeTruthy();

    passwordControl?.setValue('Admin@123');
    expect(passwordControl?.valid).toBeTrue();
  });

  it('should show error when form is submitted invalid', () => {
    component.onSubmit();
    expect(component.loginForm.get('mobileNumber')?.touched).toBeTrue();
    expect(component.loginForm.get('password')?.touched).toBeTrue();
  });

  it('should call authService.login on valid submit', fakeAsync(() => {
    const mockResponse = {
      accessToken: 'test-token',
      tokenType: 'Bearer',
      user: { id: 1, name: 'Admin', mobileNumber: '9876543210', email: 'admin@test.com', role: 'ADMIN' as const }
    };
    authService.login.and.returnValue(of(mockResponse));

    component.loginForm.setValue({ mobileNumber: '9876543210', password: 'Admin@123' });
    component.onSubmit();
    tick();

    expect(authService.login).toHaveBeenCalledWith({
      mobileNumber: '9876543210',
      password: 'Admin@123'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  }));

  it('should show error on 401 response', fakeAsync(() => {
    const errorResponse = { status: 401, error: { message: 'Invalid credentials' } };
    authService.login.and.returnValue(throwError(() => errorResponse));

    component.loginForm.setValue({ mobileNumber: '9876543210', password: 'WrongPassword' });
    component.onSubmit();
    tick();

    expect(component.errorMessage).toBe('Invalid mobile number or password');
    expect(component.isLoading).toBeFalse();
  }));

  it('should show error on 403 response', fakeAsync(() => {
    const errorResponse = { status: 403, error: { message: 'Account is not active. Please contact administrator.' } };
    authService.login.and.returnValue(throwError(() => errorResponse));

    component.loginForm.setValue({ mobileNumber: '9876543210', password: 'Admin@123' });
    component.onSubmit();
    tick();

    expect(component.errorMessage).toBe('Account is not active. Please contact administrator.');
  }));

  it('should show connection error on network failure', fakeAsync(() => {
    const errorResponse = { status: 0 };
    authService.login.and.returnValue(throwError(() => errorResponse));

    component.loginForm.setValue({ mobileNumber: '9876543210', password: 'Admin@123' });
    component.onSubmit();
    tick();

    expect(component.errorMessage).toBe('Unable to connect to server. Please try again later.');
  }));

  it('should toggle password visibility', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePasswordVisibility();
    expect(component.showPassword).toBeTrue();
    component.togglePasswordVisibility();
    expect(component.showPassword).toBeFalse();
  });
});