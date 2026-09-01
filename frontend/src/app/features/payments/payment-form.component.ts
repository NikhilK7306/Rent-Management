import { Component, inject, input, output, signal, computed, effect, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { PaymentRequest, PaymentMethod, PAYMENT_METHODS } from '@core/models/payment.model';
import { Rent } from '@core/models/rent.model';
import { PaymentService } from '@core/services/payment.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './payment-form.component.html',
  styleUrl: './payment-form.component.scss'
})
export class PaymentFormComponent implements OnInit, OnChanges {
  private paymentService = inject(PaymentService);
  private fb = inject(FormBuilder);
  private toastService = inject(ToastService);

  isOpen = input.required<boolean>();
  availableRents = input<Rent[]>([]);
  paymentToEdit = input<PaymentRequest | null>(null);
  paymentId = input<number | null>(null);
  close = output<void>();
  saved = output<void>();

  isSubmitting = signal(false);
  errorMessage = signal('');
  isEditing = signal(false);

  paymentMethods = PAYMENT_METHODS;

  form = this.fb.nonNullable.group({
    rentId: [0, Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    paymentDate: [this.getTodayString(), Validators.required],
    paymentMethod: ['CASH' as PaymentMethod, Validators.required],
    referenceNumber: [''],
    notes: ['']
  });

  selectedRent = computed(() => {
    const rentId = this.form.getRawValue().rentId;
    return this.availableRents().find(r => r.id === rentId);
  });

  outstandingAmount = computed(() => {
    const rent = this.selectedRent();
    if (!rent) return 0;
    const paid = rent.paidAmount || 0;
    return rent.monthlyRent - paid;
  });

  amountExceedsOutstanding = computed(() => {
    const amount = this.form.getRawValue().amount;
    const outstanding = this.outstandingAmount();
    return amount > outstanding;
  });

  ngOnInit(): void {
    this.form.get('rentId')?.valueChanges.subscribe(() => {
      this.updateAmountForSelectedRent();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['paymentToEdit'] || changes['paymentId']) {
      if (this.paymentToEdit() && this.paymentId()) {
        this.isEditing.set(true);
        this.populateFormForEdit();
      } else {
        this.isEditing.set(false);
        this.resetForm();
      }
    }
  }

  getTodayString(): string {
    return new Date().toISOString().split('T')[0];
  }

  private populateFormForEdit(): void {
    const payment = this.paymentToEdit();
    if (payment) {
      this.form.patchValue({
        rentId: payment.rentId,
        amount: payment.amount,
        paymentDate: payment.paymentDate,
        paymentMethod: payment.paymentMethod,
        referenceNumber: payment.referenceNumber || '',
        notes: payment.notes || ''
      });
      // Disable rent selection when editing
      this.form.get('rentId')?.disable();
    }
  }

  private updateAmountForSelectedRent(): void {
    const rent = this.selectedRent();
    if (rent && !this.isEditing()) {
      const outstanding = this.outstandingAmount();
      this.form.patchValue({ amount: outstanding });
    }
  }

  private resetForm(): void {
    this.form.reset({
      rentId: 0,
      amount: 0,
      paymentDate: this.getTodayString(),
      paymentMethod: 'CASH',
      referenceNumber: '',
      notes: ''
    });
    this.form.get('rentId')?.enable();
    this.errorMessage.set('');
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.amountExceedsOutstanding()) {
      this.errorMessage.set('Payment amount cannot exceed outstanding balance');
      return;
    }

    const request: PaymentRequest = this.form.getRawValue();

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const operation = this.isEditing() && this.paymentId()
      ? this.paymentService.updatePayment(this.paymentId()!, request)
      : this.paymentService.createPayment(request);

    operation.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success(
          this.isEditing() ? 'Payment Updated' : 'Payment Recorded',
          this.isEditing() ? 'Payment has been updated successfully' : 'Payment has been recorded successfully'
        );
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || (this.isEditing() ? 'Failed to update payment.' : 'Failed to record payment.'));
        this.toastService.error(
          this.isEditing() ? 'Update Failed' : 'Payment Failed',
          err.error?.message || (this.isEditing() ? 'Failed to update payment. Please try again.' : 'Failed to record payment. Please try again.')
        );
      }
    });
  }

  onClose(): void {
    this.resetForm();
    this.isEditing.set(false);
    this.close.emit();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1];
  }
}