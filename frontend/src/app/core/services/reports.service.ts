import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
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
} from '../models/reports.model';

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  private buildParams(params: Record<string, any>): HttpParams {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        httpParams = httpParams.set(key, value);
      }
    });
    return httpParams;
  }

  getRentReport(request: RentReportRequest): Observable<RentReportResponse> {
    const params = this.buildParams({
      month: request.month,
      year: request.year,
      fromDate: request.fromDate,
      toDate: request.toDate,
      tenantId: request.tenantId,
      propertyId: request.propertyId,
      status: request.status,
      page: request.page ?? 0,
      size: request.size ?? 20
    });
    return this.http.get<RentReportResponse>(`${this.env.apiBaseUrl}/admin/reports/rents`, { params });
  }

  getPaymentReport(request: PaymentReportRequest): Observable<PaymentReportResponse> {
    const params = this.buildParams({
      month: request.month,
      year: request.year,
      fromDate: request.fromDate,
      toDate: request.toDate,
      tenantId: request.tenantId,
      propertyId: request.propertyId,
      status: request.status,
      paymentMethod: request.paymentMethod,
      page: request.page ?? 0,
      size: request.size ?? 20
    });
    return this.http.get<PaymentReportResponse>(`${this.env.apiBaseUrl}/admin/reports/payments`, { params });
  }

  getOutstandingReport(request: OutstandingReportRequest): Observable<OutstandingReportResponse> {
    const params = this.buildParams({
      month: request.month,
      year: request.year,
      fromDate: request.fromDate,
      toDate: request.toDate,
      tenantId: request.tenantId,
      propertyId: request.propertyId,
      page: request.page ?? 0,
      size: request.size ?? 20
    });
    return this.http.get<OutstandingReportResponse>(`${this.env.apiBaseUrl}/admin/reports/outstanding`, { params });
  }

  getTenantReport(tenantId: number): Observable<TenantReportResponse> {
    return this.http.get<TenantReportResponse>(`${this.env.apiBaseUrl}/admin/reports/tenants/${tenantId}`);
  }

  getPropertyReport(propertyId: number): Observable<PropertyReportResponse> {
    return this.http.get<PropertyReportResponse>(`${this.env.apiBaseUrl}/admin/reports/properties/${propertyId}`);
  }

  getRentStatusReport(month?: number, year?: number, fromDate?: string, toDate?: string): Observable<RentStatusReportResponse> {
    const params = this.buildParams({
      month,
      year,
      fromDate,
      toDate
    });
    return this.http.get<RentStatusReportResponse>(`${this.env.apiBaseUrl}/admin/reports/rent-status`, { params });
  }
}