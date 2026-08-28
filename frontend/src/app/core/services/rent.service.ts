import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { Rent, RentRequest, RentStatusRequest, RentPage, RentStatus, BulkRentRequest, BulkRentResult, TotalOutstandingRequest, TotalOutstandingResult } from '@features/rents/rent.model';

@Injectable({ providedIn: 'root' })
export class RentService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/admin/rents`;

  getRents(search?: string, month?: number, year?: number, status?: string, overdue?: string, page = 0, size = 10, sort = 'rentYear,desc'): Observable<RentPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search) {
      params = params.set('search', search);
    }
    if (month) {
      params = params.set('month', month);
    }
    if (year) {
      params = params.set('year', year);
    }
    if (status) {
      params = params.set('status', status);
    }
    if (overdue) {
      params = params.set('overdue', overdue);
    }
    return this.http.get<RentPage>(this.baseUrl, { params });
  }

  getRent(id: number): Observable<Rent> {
    return this.http.get<Rent>(`${this.baseUrl}/${id}`);
  }

  createRent(rent: RentRequest): Observable<any> {
    return this.http.post(this.baseUrl, rent);
  }

  createHistoricalRents(request: BulkRentRequest): Observable<BulkRentResult> {
    return this.http.post<BulkRentResult>(`${this.baseUrl}/bulk`, request);
  }

  createTotalOutstandingRent(request: TotalOutstandingRequest): Observable<TotalOutstandingResult> {
    return this.http.post<TotalOutstandingResult>(`${this.baseUrl}/outstanding`, request);
  }

  updateRentStatus(id: number, status: RentStatusRequest): Observable<any> {
    return this.http.patch(`${this.baseUrl}/${id}/status`, status);
  }
}