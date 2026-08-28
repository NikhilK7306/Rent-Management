import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { Payment, PaymentRequest, PaymentPage, PaymentMethod, PaymentStatus } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/admin/payments`;

  getPayments(
    search?: string,
    rentId?: number,
    status?: string,
    paymentMethod?: string,
    startDate?: string,
    endDate?: string,
    page = 0,
    size = 10,
    sort = 'paymentDate,desc'
  ): Observable<PaymentPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search) {
      params = params.set('search', search);
    }
    if (rentId) {
      params = params.set('rentId', rentId);
    }
    if (status) {
      params = params.set('status', status);
    }
    if (paymentMethod) {
      params = params.set('paymentMethod', paymentMethod);
    }
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<PaymentPage>(this.baseUrl, { params });
  }

  getPayment(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.baseUrl}/${id}`);
  }

  createPayment(payment: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.baseUrl, payment);
  }

  updatePayment(id: number, payment: PaymentRequest): Observable<Payment> {
    return this.http.put<Payment>(`${this.baseUrl}/${id}`, payment);
  }

  deletePayment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  cancelPayment(id: number): Observable<Payment> {
    return this.http.patch<Payment>(`${this.baseUrl}/${id}/cancel`, {});
  }

  getPaymentsByRent(rentId: number, page = 0, size = 10, sort = 'paymentDate,desc'): Observable<PaymentPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    return this.http.get<PaymentPage>(`${this.baseUrl}/rent/${rentId}`, { params });
  }
}