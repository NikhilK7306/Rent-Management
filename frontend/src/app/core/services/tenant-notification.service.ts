import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { NotificationResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class TenantNotificationService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.env.apiBaseUrl}/tenant/notifications/unread-count`);
  }

  getMyNotifications(page = 0, size = 20): Observable<{ content: NotificationResponse[], totalElements: number }> {
    return this.http.get<{ content: NotificationResponse[], totalElements: number }>(
      `${this.env.apiBaseUrl}/tenant/notifications`,
      { params: { page, size } }
    );
  }

  markAsRead(id: number): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(`${this.env.apiBaseUrl}/tenant/notifications/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.env.apiBaseUrl}/tenant/notifications/read-all`, {});
  }
}