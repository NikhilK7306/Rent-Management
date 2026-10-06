import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TenantRentService } from '@core/services/tenant-rent.service';
import { TenantRentResponse, TenantRentPage } from '@core/models/tenant.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-tenant-rents',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tenant-rents.component.html',
  styleUrl: './tenant-rents.component.scss'
})
export class TenantRentsComponent implements OnInit {
  private rentService = inject(TenantRentService);

  rents = signal<TenantRentResponse[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');
  totalElements = signal(0);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = 10;

  ngOnInit(): void {
    this.loadRents();
  }

  loadRents(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.rentService.getMyRents(this.currentPage(), this.pageSize).subscribe({
      next: (data: TenantRentPage) => {
        this.rents.set(data.content);
        this.totalElements.set(data.totalElements);
        this.totalPages.set(data.totalPages);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to backend. Please check if the server is running.');
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to load rent data.');
        }
      }
    });
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadRents();
    }
  }

  formatCurrency(amount: string | null | undefined): string {
    if (!amount) return '₹0';
    const num = parseFloat(amount);
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(num);
  }

  formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  getStatusClass(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower === 'paid') return 'status-paid';
    if (statusLower === 'pending') return 'status-pending';
    if (statusLower === 'overdue') return 'status-overdue';
    if (statusLower === 'partial') return 'status-partial';
    return '';
  }

  getMonthName(month: number): string {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[month - 1] || '';
  }

  getDisplayedPages(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const pages: number[] = [];
    
    let start = Math.max(0, current - 2);
    let end = Math.min(total - 1, current + 2);
    
    if (end - start < 4) {
      if (start === 0) {
        end = Math.min(4, total - 1);
      } else if (end === total - 1) {
        start = Math.max(0, total - 5);
      }
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  parseFloatVal(val: string | null | undefined): number {
    if (!val) return 0;
    return parseFloat(val);
  }
}