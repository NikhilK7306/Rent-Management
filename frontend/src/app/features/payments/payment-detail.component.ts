import { Component, inject, input, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Payment, PaymentStatus, PaymentMethod, PAYMENT_STATUS_COLORS, PAYMENT_METHOD_COLORS } from '@core/models/payment.model';
import { RentService } from '@core/services/rent.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-payment-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-detail.component.html',
  styleUrl: './payment-detail.component.scss'
})
export class PaymentDetailComponent {
  payment = input.required<Payment | null>();
  isOpen = input.required<boolean>();
  close = output<void>();

  private rentService = inject(RentService);
  private toastService = inject(ToastService);

  getStatusColor(status: PaymentStatus): string {
    return PAYMENT_STATUS_COLORS[status] || '#6b7280';
  }

  getRentStatusColor(status: 'PENDING' | 'PARTIAL' | 'PAID' | 'OVERDUE'): string {
    const colorMap: Record<string, string> = {
      PENDING: '#f59e0b',
      PARTIAL: '#3b82f6',
      PAID: '#10b981',
      OVERDUE: '#ef4444'
    };
    return colorMap[status] || '#6b7280';
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
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1];
  }

  getRentPeriodDisplay(rent: Payment['rent']): string {
    return `${this.getMonthName(rent.rentMonth)} ${rent.rentYear}`;
  }

  getOutstanding(rent: Payment['rent']): number {
    const paid = rent.paidAmount || 0;
    return rent.monthlyRent - paid;
  }

  onClose(): void {
    this.close.emit();
  }
}