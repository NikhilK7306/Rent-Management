import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Tenant, TenantRequest } from '@core/models/tenant.model';
import { TenantService } from '@core/services/tenant.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-tenant-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tenant-form.component.html',
  styleUrl: './tenant-form.component.scss'
})
export class TenantFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private tenantService = inject(TenantService);

  isOpen = input.required<boolean>();
  tenant = input<Tenant | null>(null);
  close = output<void>();
  saved = output<void>();

  isSubmitting = signal(false);
  errorMessage = signal('');

  form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    mobileNumber: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(15), Validators.pattern('^[0-9]+$')]],
    email: ['', [Validators.email, Validators.maxLength(100)]],
    address: ['', Validators.maxLength(500)]
  });

  ngOnInit(): void {
    if (this.tenant()) {
      this.populateForm();
    }
  }

  populateForm(): void {
    const t = this.tenant()!;
    this.form.patchValue({
      fullName: t.fullName,
      mobileNumber: t.mobileNumber,
      email: t.email || '',
      address: t.address || ''
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const request: TenantRequest = this.form.getRawValue();
    const t = this.tenant();

    const operation = t
      ? this.tenantService.updateTenant(t.id, request)
      : this.tenantService.createTenant(request);

    operation.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 400 && err.error?.validationErrors) {
          const errors = err.error.validationErrors;
          Object.keys(errors).forEach(key => {
            const control = this.form.get(key);
            if (control) {
              control.setErrors({ serverError: errors[key] });
            }
          });
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to save tenant.');
        }
      }
    });
  }

  onClose(): void {
    this.close.emit();
    this.resetForm();
  }

  resetForm(): void {
    this.form.reset({
      fullName: '',
      mobileNumber: '',
      email: '',
      address: ''
    });
    this.errorMessage.set('');
  }

  getError(controlName: string): string | null {
    const control = this.form.get(controlName);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (control.errors['maxlength']) return `${this.getFieldLabel(controlName)} exceeds maximum length`;
      if (control.errors['minlength']) return `${this.getFieldLabel(controlName)} is too short`;
      if (control.errors['pattern']) return `${this.getFieldLabel(controlName)} must contain only digits`;
      if (control.errors['email']) return `${this.getFieldLabel(controlName)} is invalid`;
      if (control.errors['serverError']) return control.errors['serverError'];
    }
    return null;
  }

  getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      fullName: 'Full Name',
      mobileNumber: 'Mobile Number',
      email: 'Email',
      address: 'Address'
    };
    return labels[controlName] || controlName;
  }

  isFieldInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!(control?.touched && control?.invalid);
  }
}