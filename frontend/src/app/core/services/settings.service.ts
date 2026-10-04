import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { SessionSettings, SessionSettingsResponse } from '../models/settings.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getSessionSettings(): Observable<SessionSettingsResponse> {
    return this.http.get<SessionSettingsResponse>(`${this.env.apiBaseUrl}/admin/settings/session`);
  }

  updateSessionSettings(settings: SessionSettings): Observable<SessionSettingsResponse> {
    return this.http.put<SessionSettingsResponse>(`${this.env.apiBaseUrl}/admin/settings/session`, settings);
  }
}