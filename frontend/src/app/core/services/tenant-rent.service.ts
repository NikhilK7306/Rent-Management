import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { TenantRentResponse, TenantRentPage } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantRentService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/tenant/rents`;

  getMyRents(page = 0, size = 10, sort = 'rentYear,desc'): Observable<TenantRentPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    return this.http.get<TenantRentPage>(this.baseUrl, { params });
  }

  getCurrentMonthRent(): Observable<TenantRentResponse> {
    return this.http.get<TenantRentResponse>(`${this.baseUrl}/current`);
  }
}