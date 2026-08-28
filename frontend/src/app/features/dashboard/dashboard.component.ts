import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '@core/services/dashboard.service';
import { DashboardResponse } from '@core/models/dashboard.model';
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
}