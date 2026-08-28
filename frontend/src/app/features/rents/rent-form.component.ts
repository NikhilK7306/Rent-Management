import { Component, OnInit, inject, input, output, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Rent, RentRequest, BulkRentRequest, TotalOutstandingRequest, TotalOutstandingResult, RentStatus, RENT_STATUSES } from '@features/rents/rent.model';
import { RentService } from '@core/services/rent.service';
import { TenantService } from '@core/services/tenant.service';
import { PropertyService } from '@core/services/property.service';
import { Tenant } from '@core/models/tenant.model';
import { Property, PropertyPage } from '@core/models/property.model';
import { HttpErrorResponse } from '@angular/common/http';

export type RentFormMode = 'single' | 'historical' | 'total';

@Component({
  selector: 'app-rent-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rent-form.component.html',
  styleUrl: './rent-form.component.scss'
})
export class RentFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private rentService = inject(RentService);
  private tenantService = inject(TenantService);
  private propertyService = inject(PropertyService);

  isOpen = input.required<boolean>();
  rent = input<Rent | null>(null);
  close = output<void>();
  saved = output<void>();

  isSubmitting = signal(false);
  errorMessage = signal('');
  mode = signal<RentFormMode>('single');
  showModeSelector = signal(true);

  tenants = signal<any[]>([]);
  properties = signal<any[]>([]);
  months = Array.from({ length: 12 }, (_, i) => i + 1);
  years = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() + i);
  rentStatuses = RENT_STATUSES;

  // Single month form
  singleForm = this.fb.nonNullable.group({
    tenantId: [0, [Validators.required]],
    propertyId: [0, [Validators.required]],
    rentMonth: [0, [Validators.required, Validators.min(1), Validators.max(12)]],
    rentYear: [0, [Validators.required, Validators.min(2020), Validators.max(2100)]],
    monthlyRent: [0, [Validators.required, Validators.min(0.01)]],
    dueDate: ['', Validators.required]
  });

  // Historical form
  historicalForm = this.fb.nonNullable.group({
    tenantId: [0, [Validators.required]],
    propertyId: [0, [Validators.required]],
    startMonth: [0, [Validators.required, Validators.min(1), Validators.max(12)]],
    startYear: [0, [Validators.required, Validators.min(2020), Validators.max(2100)]],
    endMonth: [0, [Validators.required, Validators.min(1), Validators.max(12)]],
    endYear: [0, [Validators.required, Validators.min(2020), Validators.max(2100)]],
    monthlyRent: [0, [Validators.required, Validators.min(0.01)]],
    dueDate: [''],
    initialStatus: ['PENDING' as RentStatus, Validators.required]
  });

  // Total outstanding form
  totalForm = this.fb.nonNullable.group({
    tenantId: [0, [Validators.required]],
    propertyId: [0, [Validators.required]],
    asOfDate: ['', Validators.required],
    totalOutstandingAmount: [0, [Validators.required, Validators.min(0.01)]],
    numberOfMonths: [0],
    notes: ['']
  });

  // Computed values for historical form
  totalMonths = computed(() => {
    const startMonth = this.historicalForm.get('startMonth')?.value || 0;
    const startYear = this.historicalForm.get('startYear')?.value || 0;
    const endMonth = this.historicalForm.get('endMonth')?.value || 0;
    const endYear = this.historicalForm.get('endYear')?.value || 0;

    if (!startMonth || !startYear || !endMonth || !endYear) return 0;

    return (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
  });

  totalAmount = computed(() => {
    const monthlyRent = this.historicalForm.get('monthlyRent')?.value || 0;
    const months = this.totalMonths();
    return monthlyRent * months;
  });

  // Computed values for total form
  monthlyAmount = computed(() => {
    const total = this.totalForm.get('totalOutstandingAmount')?.value || 0;
    const months = this.totalForm.get('numberOfMonths')?.value || 0;
    if (!months || months <= 0) return 0;
    return Math.round((total / months) * 100) / 100;
  });

  lastMonthAmount = computed(() => {
    const total = this.totalForm.get('totalOutstandingAmount')?.value || 0;
    const months = this.totalForm.get('numberOfMonths')?.value || 0;
    if (!months || months <= 1) return total;
    const monthly = this.monthlyAmount();
    const distributed = monthly * (months - 1);
    return Math.round((total - distributed) * 100) / 100;
  });

  ngOnInit(): void {
    this.loadTenants();
    this.loadProperties();

    // Reset forms when mode changes
    effect(() => {
      const currentMode = this.mode();
      this.resetCurrentForm();
    });

    if (this.rent()) {
      this.populateForm();
    }
  }

  loadTenants(): void {
    this.tenantService.getTenants(undefined, 'ACTIVE', 0, 100).subscribe({
      next: (response) => {
        this.tenants.set(response.content);
      },
      error: (err) => console.error('Failed to load tenants:', err)
    });
  }

  loadProperties(): void {
    this.propertyService.getProperties(undefined, 'ACTIVE', 0, 100).subscribe({
      next: (response) => {
        this.properties.set(response.content);
      },
      error: (err) => console.error('Failed to load properties:', err)
    });
  }

  populateForm(): void {
    const rent = this.rent()!;
    if (this.mode() === 'single') {
      this.singleForm.patchValue({
        tenantId: rent.tenant.id,
        propertyId: rent.property.id,
        rentMonth: rent.rentMonth,
        rentYear: rent.rentYear,
        monthlyRent: rent.monthlyRent,
        dueDate: rent.dueDate
      });
    }
  }

  setMode(mode: RentFormMode): void {
    this.mode.set(mode);
  }

  onSubmit(): void {
    const currentForm = this.getCurrentForm();
    if (currentForm.invalid) {
      currentForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const mode = this.mode();

    if (mode === 'single') {
      this.submitSingleRent();
    } else if (mode === 'historical') {
      this.submitHistoricalRent();
    } else if (mode === 'total') {
      this.submitTotalOutstanding();
    }
  }

  private submitSingleRent(): void {
    const request = this.singleForm.getRawValue();
    this.rentService.createRent(request).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 400 && err.error?.validationErrors) {
          const errors = err.error.validationErrors;
          Object.keys(errors).forEach(key => {
            const control = this.singleForm.get(key);
            if (control) {
              control.setErrors({ serverError: errors[key] });
            }
          });
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to create rent record.');
        }
      }
    });
  }

  private submitHistoricalRent(): void {
    const request: BulkRentRequest = this.historicalForm.getRawValue();
    this.rentService.createHistoricalRents(request).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        let message = `Successfully created ${result.createdCount} rent record(s).`;
        if (result.skippedCount > 0) {
          message += ` ${result.skippedCount} record(s) skipped (already exist).`;
        }
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 400 && err.error?.validationErrors) {
          const errors = err.error.validationErrors;
          Object.keys(errors).forEach(key => {
            const control = this.historicalForm.get(key);
            if (control) {
              control.setErrors({ serverError: errors[key] });
            }
          });
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to create historical rent records.');
        }
      }
    });
  }

  private submitTotalOutstanding(): void {
    const request: TotalOutstandingRequest = this.totalForm.getRawValue();
    this.rentService.createTotalOutstandingRent(request).subscribe({
      next: (result: TotalOutstandingResult) => {
        this.isSubmitting.set(false);
        let message = `Successfully created ${result.isConsolidated ? 'consolidated arrears record' : `${result.createdCount} rent record(s)`}.`;
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 400 && err.error?.validationErrors) {
          const errors = err.error.validationErrors;
          Object.keys(errors).forEach(key => {
            const control = this.totalForm.get(key);
            if (control) {
              control.setErrors({ serverError: errors[key] });
            }
          });
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to create outstanding rent record.');
        }
      }
    });
  }

  onClose(): void {
    this.close.emit();
    this.resetForms();
  }

  resetForms(): void {
    this.singleForm.reset({
      tenantId: 0,
      propertyId: 0,
      rentMonth: 0,
      rentYear: 0,
      monthlyRent: 0,
      dueDate: ''
    });

    this.historicalForm.reset({
      tenantId: 0,
      propertyId: 0,
      startMonth: 0,
      startYear: 0,
      endMonth: 0,
      endYear: 0,
      monthlyRent: 0,
      dueDate: '',
      initialStatus: 'PENDING'
    });

    this.totalForm.reset({
      tenantId: 0,
      propertyId: 0,
      asOfDate: '',
      totalOutstandingAmount: 0,
      numberOfMonths: 0,
      notes: ''
    });

    this.errorMessage.set('');
  }

  private resetCurrentForm(): void {
    this.singleForm.reset({
      tenantId: 0,
      propertyId: 0,
      rentMonth: 0,
      rentYear: 0,
      monthlyRent: 0,
      dueDate: ''
    });

    this.historicalForm.reset({
      tenantId: 0,
      propertyId: 0,
      startMonth: 0,
      startYear: 0,
      endMonth: 0,
      endYear: 0,
      monthlyRent: 0,
      dueDate: '',
      initialStatus: 'PENDING'
    });

    this.totalForm.reset({
      tenantId: 0,
      propertyId: 0,
      asOfDate: '',
      totalOutstandingAmount: 0,
      numberOfMonths: 0,
      notes: ''
    });
  }

  getCurrentForm() {
    const mode = this.mode();
    if (mode === 'single') return this.singleForm;
    if (mode === 'historical') return this.historicalForm;
    return this.totalForm;
  }

  getError(controlName: string, form?: any): string | null {
    const formToCheck = (form || this.getCurrentForm()) as any;
    const control = formToCheck.get(controlName);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (control.errors['min']) return `${this.getFieldLabel(controlName)} must be at least ${control.errors['min'].min}`;
      if (control.errors['max']) return `${this.getFieldLabel(controlName)} must not exceed ${control.errors['max'].max}`;
      if (control.errors['serverError']) return control.errors['serverError'];
    }
    return null;
  }

  getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      tenantId: 'Tenant',
      propertyId: 'Property',
      rentMonth: 'Rent Month',
      rentYear: 'Rent Year',
      monthlyRent: 'Monthly Rent',
      dueDate: 'Due Date',
      startMonth: 'Start Month',
      startYear: 'Start Year',
      endMonth: 'End Month',
      endYear: 'End Year',
      asOfDate: 'As Of Date',
      totalOutstandingAmount: 'Total Outstanding Amount',
      numberOfMonths: 'Number of Months',
      notes: 'Notes'
    };
    return labels[controlName] || controlName;
  }

  getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1];
  }

  isFieldInvalid(controlName: string): boolean {
    const form = this.getCurrentForm() as any;
    const control = form.get(controlName);
    return !!(control?.touched && control?.invalid);
  }

  getCurrentModeLabel(): string {
    switch (this.mode()) {
      case 'single': return 'Single Month Rent';
      case 'historical': return 'Historical / Arrears Rent';
      case 'total': return 'Total Outstanding Amount';
      default: return 'Add Rent';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  isFieldInvalidForForm(controlName: string, form: any): boolean {
    const control = (form as any).get(controlName);
    return !!(control?.touched && control?.invalid);
  }

  getFormForMode(mode: RentFormMode) {
    switch (mode) {
      case 'single': return this.singleForm;
      case 'historical': return this.historicalForm;
      case 'total': return this.totalForm;
    }
  }
}