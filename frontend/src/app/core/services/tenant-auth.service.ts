import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { TenantMeResponse } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantAuthService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/tenant`;

  getMe(): Observable<TenantMeResponse> {
    return this.http.get<TenantMeResponse>(`${this.baseUrl}/me`);
  }
}