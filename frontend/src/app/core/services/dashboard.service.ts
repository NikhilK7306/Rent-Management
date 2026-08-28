import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { DashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.env.apiBaseUrl}/admin/dashboard`);
  }
}