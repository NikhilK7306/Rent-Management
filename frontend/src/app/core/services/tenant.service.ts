import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '@core/services/environment.token';
import { Tenant, TenantRequest, TenantStatusRequest, TenantPropertyRequest, TenantPage } from '@core/models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/admin/tenants`;

  getTenants(search?: string, status?: string, page = 0, size = 10, sort = 'createdAt,desc'): Observable<TenantPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search) {
      params = params.set('search', search);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<TenantPage>(this.baseUrl, { params });
  }

  getTenant(id: number): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.baseUrl}/${id}`);
  }

  createTenant(tenant: TenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.baseUrl, tenant);
  }

  updateTenant(id: number, tenant: TenantRequest): Observable<Tenant> {
    return this.http.put<Tenant>(`${this.baseUrl}/${id}`, tenant);
  }

  updateTenantStatus(id: number, status: TenantStatusRequest): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.baseUrl}/${id}/status`, status);
  }

  assignProperty(tenantId: number, request: TenantPropertyRequest): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.baseUrl}/${tenantId}/property`, request);
  }

  unassignProperty(tenantId: number): Observable<Tenant> {
    return this.http.delete<Tenant>(`${this.baseUrl}/${tenantId}/property`);
  }
}