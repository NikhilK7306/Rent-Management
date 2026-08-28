import { Component, OnInit, inject, input, output, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Property, PropertyRequest, PropertyType, PROPERTY_TYPES } from '@core/models/property.model';
import { PropertyService } from '@core/services/property.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-property-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './property-form.component.html',
  styleUrl: './property-form.component.scss'
})
export class PropertyFormComponent {
  private fb = inject(FormBuilder);
  private propertyService = inject(PropertyService);

  isOpen = input.required<boolean>();
  property = input<Property | null>(null);
  close = output<void>();
  saved = output<void>();

  isSubmitting = signal(false);
  errorMessage = signal('');

  propertyTypes = PROPERTY_TYPES;

  form = this.fb.nonNullable.group({
    propertyName: ['', [Validators.required, Validators.maxLength(100)]],
    propertyCode: ['', [Validators.required, Validators.maxLength(20)]],
    propertyType: ['HOUSE' as PropertyType, Validators.required],
    address: ['', [Validators.required, Validators.maxLength(500)]],
    description: ['', Validators.maxLength(1000)],
    monthlyRent: [0, [Validators.required, Validators.min(0.01)]]
  });

  constructor() {
    effect(() => {
      const prop = this.property();
      if (prop) {
        this.populateForm();
      } else {
        this.resetForm();
      }
    });
  }

  populateForm(): void {
    const prop = this.property()!;
    this.form.patchValue({
      propertyName: prop.propertyName,
      propertyCode: prop.propertyCode,
      propertyType: prop.propertyType,
      address: prop.address,
      description: prop.description || '',
      monthlyRent: prop.monthlyRent
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const request: PropertyRequest = this.form.getRawValue();
    const prop = this.property();

    const operation = prop
      ? this.propertyService.updateProperty(prop.id, request)
      : this.propertyService.createProperty(request);

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
          this.errorMessage.set(err.error?.message || 'Failed to save property.');
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
      propertyName: '',
      propertyCode: '',
      propertyType: 'HOUSE' as PropertyType,
      address: '',
      description: '',
      monthlyRent: 0
    });
    this.errorMessage.set('');
  }

  getError(controlName: string): string | null {
    const control = this.form.get(controlName);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (control.errors['maxlength']) return `${this.getFieldLabel(controlName)} exceeds maximum length`;
      if (control.errors['min']) return `${this.getFieldLabel(controlName)} must be greater than zero`;
      if (control.errors['serverError']) return control.errors['serverError'];
    }
    return null;
  }

  getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      propertyName: 'Property Name',
      propertyCode: 'Property Code',
      propertyType: 'Property Type',
      address: 'Address',
      description: 'Description',
      monthlyRent: 'Monthly Rent'
    };
    return labels[controlName] || controlName;
  }

  isFieldInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!(control?.touched && control?.invalid);
  }
}