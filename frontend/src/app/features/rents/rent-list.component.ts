import { Component, OnInit, inject, input, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RentService } from '@core/services/rent.service';
import { TenantService } from '@core/services/tenant.service';
import { PropertyService } from '@core/services/property.service';
import { Rent, RentPage, RentStatus, RENT_STATUSES } from '@features/rents/rent.model';
import { Property, PropertyPage } from '@core/models/property.model';
import { HttpErrorResponse } from '@angular/common/http';
import { RentFormComponent } from './rent-form.component';
import { RentDetailComponent } from './rent-detail.component';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-rent-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, RentFormComponent, RentDetailComponent],
  templateUrl: './rent-list.component.html',
  styleUrl: './rent-list.component.scss'
})
export class RentListComponent implements OnInit {
  private rentService = inject(RentService);
  private router = inject(Router);

  rents = signal<Rent[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');
  searchTerm = signal('');
  monthFilter = signal<number | null>(null);
  yearFilter = signal<number | null>(null);
  statusFilter = signal<string>('');
  overdueFilter = signal<string>('');
  currentPage = signal(0);
  pageSize = 10;
  totalElements = signal(0);
  totalPages = signal(0);

  showAddModal = signal(false);
  showDetailModal = signal(false);
  selectedRent = signal<any | null>(null);

  statuses = RENT_STATUSES;
  months = Array.from({ length: 12 }, (_, i) => i + 1);
  years = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - 5 + i);

  filteredRents = computed(() => this.rents());

  ngOnInit(): void {
    this.loadRents();
    this.loadCurrentMonthYear();
  }

  loadCurrentMonthYear(): void {
    const now = new Date();
    this.monthFilter.set(now.getMonth() + 1);
    this.yearFilter.set(now.getFullYear());
  }

  loadRents(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.rentService.getRents(
      this.searchTerm() || undefined,
      this.monthFilter() || undefined,
      this.yearFilter() || undefined,
      this.statusFilter() || undefined,
      this.overdueFilter() || undefined,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.rents.set(response.content);
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
          this.errorMessage.set(err.error?.message || 'Failed to load rents.');
        }
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadRents();
  }

  onMonthFilterChange(): void {
    this.currentPage.set(0);
    this.loadRents();
  }

  onYearFilterChange(): void {
    this.currentPage.set(0);
    this.loadRents();
  }

  onStatusFilterChange(): void {
    this.currentPage.set(0);
    this.loadRents();
  }

  onOverdueFilterChange(): void {
    this.currentPage.set(0);
    this.loadRents();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.monthFilter.set(null);
    this.yearFilter.set(null);
    this.statusFilter.set('');
    this.overdueFilter.set('');
    this.currentPage.set(0);
    this.loadRents();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadRents();
    }
  }

  openAddModal(): void {
    this.showAddModal.set(true);
  }

  closeAddModal(): void {
    this.showAddModal.set(false);
  }

  openDetailModal(rent: any): void {
    this.selectedRent.set(rent);
    this.showDetailModal.set(true);
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedRent.set(null);
  }

  onRentCreated(): void {
    this.closeAddModal();
    this.loadRents();
  }

  onRentUpdated(): void {
    this.closeDetailModal();
    this.loadRents();
  }

  onStatusChange(rent: any, newStatus: string): void {
    if (confirm(`Are you sure you want to mark this rent as ${newStatus}?`)) {
      this.updateRentStatus(rent.id, newStatus);
    }
  }

  updateRentStatus(rentId: number, newStatus: string): void {
    this.rentService.updateRentStatus(rentId, { status: newStatus as any }).subscribe({
      next: () => {
        this.loadRents();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message || 'Failed to update rent status.');
      }
    });
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
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

  getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1];
  }

  isOverdue(rent: any): boolean {
    return rent.status === 'OVERDUE' && rent.status !== 'PAID';
  }

  getOutstanding(rent: any): number {
    const paid = rent.paidAmount || 0;
    return rent.monthlyRent - paid;
  }

  getPaid(rent: any): number {
    return rent.paidAmount || 0;
  }
}