import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TenantDashboardService } from '@core/services/tenant-dashboard.service';
import { TenantDashboardResponse } from '@core/models/tenant.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-tenant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tenant-dashboard.component.html',
  styleUrl: './tenant-dashboard.component.scss'
})
export class TenantDashboardComponent implements OnInit {
  private dashboardService = inject(TenantDashboardService);

  dashboardData: TenantDashboardResponse | null = null;
  isLoading = true;
  errorMessage = '';
  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.dashboardService.getDashboard().subscribe({
      next: (data: TenantDashboardResponse) => {
        this.dashboardData = data;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Session expired. Please log in again.';
        } else if (err.status === 0) {
          this.errorMessage = 'Unable to connect to backend. Please check if the server is running.';
        } else {
          this.errorMessage = err.error?.message || 'Failed to load dashboard data.';
        }
      }
    });
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

  getStatusClass(status: string | undefined): string {
    if (!status) return '';
    const statusLower = status.toLowerCase();
    if (statusLower === 'paid') return 'status-paid';
    if (statusLower === 'pending') return 'status-pending';
    if (statusLower === 'overdue') return 'status-overdue';
    if (statusLower === 'partial') return 'status-partial';
    return '';
  }

  getMonthName(month: number): string {
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 
                   'July', 'August', 'September', 'October', 'November', 'December'];
    return months[month - 1] || '';
  }
}