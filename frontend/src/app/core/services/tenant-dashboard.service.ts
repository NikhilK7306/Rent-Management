import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { TenantDashboardResponse } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantDashboardService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/tenant/dashboard`;

  getDashboard(): Observable<TenantDashboardResponse> {
    return this.http.get<TenantDashboardResponse>(this.baseUrl);
  }
}