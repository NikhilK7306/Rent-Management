import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '@core/services/auth.service';
import { LoginRequest } from '@core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  private readonly mobilePattern = '^[0-9]{10}$';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    if (this.authService.isAuthenticated) {
      this.router.navigate(['/dashboard']);
    }
  }

  private initForm(): void {
    this.loginForm = this.fb.group({
      mobileNumber: ['', [Validators.required, Validators.pattern(this.mobilePattern)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get mobileNumber() { return this.loginForm.get('mobileNumber'); }
  get password() { return this.loginForm.get('password'); }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials: LoginRequest = {
      mobileNumber: this.mobileNumber?.value,
      password: this.password?.value
    };

    this.authService.login(credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Invalid mobile number or password';
        } else if (err.status === 403) {
          this.errorMessage = err.error?.message || 'Account is not active. Please contact administrator.';
        } else if (err.status === 0) {
          this.errorMessage = 'Unable to connect to server. Please try again later.';
        } else {
          this.errorMessage = err.error?.message || 'An unexpected error occurred. Please try again.';
        }
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}