import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '@core/services/dashboard.service';
import { DashboardResponse, DashboardSummaryResponse } from '@core/models/dashboard.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);

  dashboardData: DashboardResponse | null = null;
  summaryData: DashboardSummaryResponse | null = null;
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.dashboardService.getDashboard().subscribe({
      next: (data: DashboardResponse) => {
        this.dashboardData = data;
        this.loadSummary();
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

  loadSummary(): void {
    this.dashboardService.getDashboardSummary().subscribe({
      next: (data: DashboardSummaryResponse) => {
        this.summaryData = data;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Session expired. Please log in again.';
        } else if (err.status === 0) {
          this.errorMessage = 'Unable to connect to backend. Please check if the server is running.';
        } else {
          this.errorMessage = err.error?.message || 'Failed to load dashboard summary.';
        }
      }
    });
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
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[month - 1] || '';
  }
}