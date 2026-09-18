import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReportsService } from '@core/services/reports.service';
import {
  RentReportRequest,
  RentReportResponse,
  PaymentReportRequest,
  PaymentReportResponse,
  OutstandingReportRequest,
  OutstandingReportResponse,
  TenantReportResponse,
  PropertyReportResponse,
  RentStatusReportResponse
} from '@core/models/reports.model';
import { HttpErrorResponse } from '@angular/common/http';

type ReportTab = 'rents' | 'payments' | 'outstanding' | 'tenant' | 'property';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  private reportsService = inject(ReportsService);

  activeTab = signal<ReportTab>('rents');
  isLoading = signal(false);
  errorMessage = signal('');

  // Filter values
  rentFilters = signal<RentReportRequest>({ page: 0, size: 20 });
  paymentFilters = signal<PaymentReportRequest>({ page: 0, size: 20 });
  outstandingFilters = signal<OutstandingReportRequest>({ page: 0, size: 20 });
  selectedTenantId = signal<number | null>(null);
  selectedPropertyId = signal<number | null>(null);

  // Report data
  rentReport = signal<RentReportResponse | null>(null);
  paymentReport = signal<PaymentReportResponse | null>(null);
  outstandingReport = signal<OutstandingReportResponse | null>(null);
  tenantReport = signal<TenantReportResponse | null>(null);
  propertyReport = signal<PropertyReportResponse | null>(null);
  rentStatusReport = signal<RentStatusReportResponse | null>(null);

  ngOnInit(): void {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y >= currentYear - 5; y--) {
      this.years.push(y);
    }
    this.loadRentReport();
    this.loadRentStatusReport();
  }

  setTab(tab: ReportTab): void {
    this.activeTab.set(tab);
    this.errorMessage.set('');

    switch (tab) {
      case 'rents':
        if (!this.rentReport()) this.loadRentReport();
        break;
      case 'payments':
        if (!this.paymentReport()) this.loadPaymentReport();
        break;
      case 'outstanding':
        if (!this.outstandingReport()) this.loadOutstandingReport();
        break;
      case 'tenant':
        break;
      case 'property':
        break;
    }
  }

  loadRentReport(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportsService.getRentReport(this.rentFilters()).subscribe({
      next: (data: RentReportResponse) => {
        this.rentReport.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.handleError(err);
      }
    });
  }

  loadPaymentReport(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportsService.getPaymentReport(this.paymentFilters()).subscribe({
      next: (data: PaymentReportResponse) => {
        this.paymentReport.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.handleError(err);
      }
    });
  }

  loadOutstandingReport(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportsService.getOutstandingReport(this.outstandingFilters()).subscribe({
      next: (data: OutstandingReportResponse) => {
        this.outstandingReport.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.handleError(err);
      }
    });
  }

  loadTenantReport(): void {
    if (!this.selectedTenantId()) {
      this.errorMessage.set('Please enter a Tenant ID');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportsService.getTenantReport(this.selectedTenantId()!).subscribe({
      next: (data: TenantReportResponse) => {
        this.tenantReport.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.handleError(err);
      }
    });
  }

  loadPropertyReport(): void {
    if (!this.selectedPropertyId()) {
      this.errorMessage.set('Please enter a Property ID');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportsService.getPropertyReport(this.selectedPropertyId()!).subscribe({
      next: (data: PropertyReportResponse) => {
        this.propertyReport.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.handleError(err);
      }
    });
  }

  loadRentStatusReport(): void {
    const filters = this.rentFilters();
    this.reportsService.getRentStatusReport(
      filters.month, filters.year, filters.fromDate, filters.toDate
    ).subscribe({
      next: (data: RentStatusReportResponse) => {
        this.rentStatusReport.set(data);
      },
      error: () => {
        // Silently fail for status report
      }
    });
  }

  onRentFiltersChange(): void {
    this.rentFilters.update(f => ({ ...f, page: 0 }));
    this.loadRentReport();
    this.loadRentStatusReport();
  }

  onPaymentFiltersChange(): void {
    this.paymentFilters.update(f => ({ ...f, page: 0 }));
    this.loadPaymentReport();
  }

  onOutstandingFiltersChange(): void {
    this.outstandingFilters.update(f => ({ ...f, page: 0 }));
    this.loadOutstandingReport();
  }

  clearFilters(): void {
    this.rentFilters.set({ page: 0, size: 20 });
    this.paymentFilters.set({ page: 0, size: 20 });
    this.outstandingFilters.set({ page: 0, size: 20 });
    this.loadRentReport();
    this.loadPaymentReport();
    this.loadOutstandingReport();
    this.loadRentStatusReport();
  }

  onRentPageChange(page: number): void {
    this.rentFilters.update(f => ({ ...f, page }));
    this.loadRentReport();
  }

  onPaymentPageChange(page: number): void {
    this.paymentFilters.update(f => ({ ...f, page }));
    this.loadPaymentReport();
  }

  onOutstandingPageChange(page: number): void {
    this.outstandingFilters.update(f => ({ ...f, page }));
    this.loadOutstandingReport();
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === 401) {
      this.errorMessage.set('Session expired. Please log in again.');
    } else if (err.status === 0) {
      this.errorMessage.set('Unable to connect to backend. Please check if the server is running.');
    } else {
      this.errorMessage.set(err.error?.message || 'Failed to load report data.');
    }
  }

  hasCurrentReportData(): boolean {
    const tab = this.activeTab();
    switch (tab) {
      case 'rents': return !!this.rentReport();
      case 'payments': return !!this.paymentReport();
      case 'outstanding': return !!this.outstandingReport();
      case 'tenant': return !!this.tenantReport();
      case 'property': return !!this.propertyReport();
      default: return false;
    }
  }

  retryCurrentTab(): void {
    const tab = this.activeTab();
    switch (tab) {
      case 'rents': this.loadRentReport(); break;
      case 'payments': this.loadPaymentReport(); break;
      case 'outstanding': this.loadOutstandingReport(); break;
      case 'tenant': this.loadTenantReport(); break;
      case 'property': this.loadPropertyReport(); break;
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

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  getStatusBadgeClass(status: string): string {
    const normalized = status.toUpperCase();
    if (normalized === 'PAID') return 'status-badge paid';
    if (normalized === 'PARTIAL') return 'status-badge partial';
    return 'status-badge pending';
  }

  getPaymentStatusBadgeClass(status: string): string {
    const normalized = status.toUpperCase();
    if (normalized === 'PAID') return 'status-badge paid';
    if (normalized === 'PARTIAL') return 'status-badge partial';
    if (normalized === 'CANCELLED') return 'status-badge cancelled';
    return 'status-badge pending';
  }

  months = [
    { value: 1, label: 'January' },
    { value: 2, label: 'February' },
    { value: 3, label: 'March' },
    { value: 4, label: 'April' },
    { value: 5, label: 'May' },
    { value: 6, label: 'June' },
    { value: 7, label: 'July' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'October' },
    { value: 11, label: 'November' },
    { value: 12, label: 'December' }
  ];

  years: number[] = [];

  getTotalPages(totalRecords: number, size: number): number {
    return Math.ceil(totalRecords / size);
  }

  getPageNumbers(currentPage: number, totalPages: number): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  getOutstandingTotalRecords(): number {
    return this.outstandingReport()?.totalRecords ?? 0;
  }
}