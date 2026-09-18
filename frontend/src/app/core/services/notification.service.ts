import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { NotificationResponse, NotificationSummaryResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getAllNotifications(page = 0, size = 20): Observable<{ content: NotificationResponse[], totalElements: number }> {
    return this.http.get<{ content: NotificationResponse[], totalElements: number }>(
      `${this.env.apiBaseUrl}/admin/notifications`,
      { params: { page, size } }
    );
  }

  getUnreadNotifications(page = 0, size = 20): Observable<{ content: NotificationResponse[], totalElements: number }> {
    return this.http.get<{ content: NotificationResponse[], totalElements: number }>(
      `${this.env.apiBaseUrl}/admin/notifications/unread`,
      { params: { page, size } }
    );
  }

  getNotificationsByTenant(tenantId: number, page = 0, size = 20): Observable<{ content: NotificationResponse[], totalElements: number }> {
    return this.http.get<{ content: NotificationResponse[], totalElements: number }>(
      `${this.env.apiBaseUrl}/admin/notifications/tenant/${tenantId}`,
      { params: { page, size } }
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.env.apiBaseUrl}/admin/notifications/unread-count`);
  }

  getNotificationSummary(): Observable<NotificationSummaryResponse> {
    return this.http.get<NotificationSummaryResponse>(`${this.env.apiBaseUrl}/admin/notifications/summary`);
  }

  markAsRead(id: number): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(`${this.env.apiBaseUrl}/admin/notifications/${id}/read`, {});
  }

  markAsReadMultiple(ids: number[]): Observable<void> {
    return this.http.put<void>(`${this.env.apiBaseUrl}/admin/notifications/read`, ids);
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.env.apiBaseUrl}/admin/notifications/read-all`, {});
  }
}