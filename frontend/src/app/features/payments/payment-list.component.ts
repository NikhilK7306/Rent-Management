import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PaymentService } from '@core/services/payment.service';
import { RentService } from '@core/services/rent.service';
import { Payment, PaymentPage, PaymentStatus, PaymentMethod, PAYMENT_STATUSES, PAYMENT_METHODS, PAYMENT_STATUS_COLORS, PAYMENT_METHOD_COLORS } from '@core/models/payment.model';
import { PaymentRequest } from '@core/models/payment.model';
import { Rent, RentPage } from '@features/rents/rent.model';
import { HttpErrorResponse } from '@angular/common/http';
import { PaymentFormComponent } from './payment-form.component';
import { PaymentDetailComponent } from './payment-detail.component';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-payment-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PaymentFormComponent, PaymentDetailComponent],
  templateUrl: './payment-list.component.html',
  styleUrl: './payment-list.component.scss'
})
export class PaymentListComponent implements OnInit {
  private paymentService = inject(PaymentService);
  private rentService = inject(RentService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  payments = signal<Payment[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');
  searchTerm = signal('');
  statusFilter = signal<string>('');
  paymentMethodFilter = signal<string>('');
  startDateFilter = signal<string>('');
  endDateFilter = signal<string>('');
  currentPage = signal(0);
  pageSize = 10;
  totalElements = signal(0);
  totalPages = signal(0);

  showAddModal = signal(false);
  showDetailModal = signal(false);
  showEditModal = signal(false);
  selectedPayment = signal<Payment | null>(null);
  selectedPaymentForEdit = signal<Payment | null>(null);

  availableRents = signal<Rent[]>([]);
  rentsLoading = signal(false);

  statuses = PAYMENT_STATUSES;
  paymentMethods = PAYMENT_METHODS;

  // Convert Payment to PaymentRequest for the edit form
  paymentToEdit = computed<PaymentRequest | null>(() => {
    const p = this.selectedPaymentForEdit();
    if (!p) return null;
    return {
      rentId: p.rent.id,
      amount: p.amount,
      paymentDate: p.paymentDate,
      paymentMethod: p.paymentMethod,
      referenceNumber: p.referenceNumber || undefined,
      notes: p.notes || undefined
    };
  });

  // Get the payment ID for editing
  paymentEditId = computed<number | null>(() => this.selectedPaymentForEdit()?.id ?? null);

  filteredPayments = computed(() => this.payments());

  ngOnInit(): void {
    this.loadPayments();
    this.loadAvailableRents();
  }

  loadPayments(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.paymentService.getPayments(
      this.searchTerm() || undefined,
      undefined,
      this.statusFilter() || undefined,
      this.paymentMethodFilter() || undefined,
      this.startDateFilter() || undefined,
      this.endDateFilter() || undefined,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (response: PaymentPage) => {
        this.payments.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to backend. Please check if the server is running.');
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to load payments.');
        }
      }
    });
  }

  loadAvailableRents(): void {
    this.rentsLoading.set(true);
    this.rentService.getRents(undefined, undefined, undefined, undefined, undefined, 0, 1000).subscribe({
      next: (response: RentPage) => {
        // Filter rents that have outstanding balance
        this.availableRents.set(response.content.filter(r => {
          const paid = r.paidAmount || 0;
          return paid < r.monthlyRent;
        }));
        this.rentsLoading.set(false);
      },
      error: () => {
        this.rentsLoading.set(false);
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadPayments();
  }

  onStatusFilterChange(): void {
    this.currentPage.set(0);
    this.loadPayments();
  }

  onPaymentMethodFilterChange(): void {
    this.currentPage.set(0);
    this.loadPayments();
  }

  onDateFilterChange(): void {
    this.currentPage.set(0);
    this.loadPayments();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('');
    this.paymentMethodFilter.set('');
    this.startDateFilter.set('');
    this.endDateFilter.set('');
    this.currentPage.set(0);
    this.loadPayments();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadPayments();
    }
  }

  openAddModal(): void {
    this.showAddModal.set(true);
  }

  closeAddModal(): void {
    this.showAddModal.set(false);
  }

  openEditModal(payment: Payment): void {
    this.selectedPaymentForEdit.set(payment);
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.selectedPaymentForEdit.set(null);
  }

  openDetailModal(payment: Payment): void {
    this.selectedPayment.set(payment);
    this.showDetailModal.set(true);
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedPayment.set(null);
  }

  onPaymentCreated(): void {
    this.closeAddModal();
    this.loadPayments();
    this.toastService.success('Payment Recorded', 'Payment has been recorded successfully');
  }

  onPaymentUpdated(): void {
    this.closeEditModal();
    this.loadPayments();
    this.toastService.success('Payment Updated', 'Payment has been updated successfully');
  }

  getStatusColor(status: PaymentStatus): string {
    return PAYMENT_STATUS_COLORS[status] || '#6b7280';
  }

  getMethodColor(method: PaymentMethod): string {
    return PAYMENT_METHOD_COLORS[method] || '#6b7280';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getRentPeriodDisplay(rent: Payment['rent']): string {
    const months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return `${months[rent.rentMonth - 1]} ${rent.rentYear}`;
  }

  getOutstanding(rent: Payment['rent']): number {
    const paid = rent.paidAmount || 0;
    return rent.monthlyRent - paid;
  }
}