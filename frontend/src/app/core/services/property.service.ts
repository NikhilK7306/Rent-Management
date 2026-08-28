import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../services/environment.token';
import { Property, PropertyRequest, PropertyStatusRequest, PropertyPage } from '../models/property.model';

@Injectable({ providedIn: 'root' })
export class PropertyService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/admin/properties`;

  getProperties(search?: string, status?: string, page = 0, size = 10, sort = 'createdAt,desc'): Observable<PropertyPage> {
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
    return this.http.get<PropertyPage>(this.baseUrl, { params });
  }

  getProperty(id: number): Observable<Property> {
    return this.http.get<Property>(`${this.baseUrl}/${id}`);
  }

  createProperty(property: PropertyRequest): Observable<Property> {
    return this.http.post<Property>(this.baseUrl, property);
  }

  updateProperty(id: number, property: PropertyRequest): Observable<Property> {
    return this.http.put<Property>(`${this.baseUrl}/${id}`, property);
  }

  updatePropertyStatus(id: number, status: PropertyStatusRequest): Observable<Property> {
    return this.http.patch<Property>(`${this.baseUrl}/${id}/status`, status);
  }
}