import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PropertyListComponent } from './property-list.component';
import { PropertyService } from '@core/services/property.service';
import { Property, PropertyPage } from '@core/models/property.model';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';

describe('PropertyListComponent', () => {
  let component: PropertyListComponent;
  let fixture: ComponentFixture<PropertyListComponent>;
  let mockPropertyService: jasmine.SpyObj<PropertyService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockProperties: Property[] = [
    {
      id: 1,
      propertyName: 'House A',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Thrissur, Kerala',
      description: 'Two bedroom rental house',
      monthlyRent: 10000,
      status: 'ACTIVE',
      createdAt: '2024-01-15T10:00:00',
      updatedAt: '2024-01-15T10:00:00',
      tenantName: 'Not Assigned',
      tenant: null
    },
    {
      id: 2,
      propertyName: 'Apartment B',
      propertyCode: 'PROP-002',
      propertyType: 'APARTMENT',
      address: 'Kochi, Kerala',
      description: null,
      monthlyRent: 15000,
      status: 'INACTIVE',
      createdAt: '2024-01-16T10:00:00',
      updatedAt: '2024-01-16T10:00:00',
      tenantName: 'Not Assigned',
      tenant: null
    }
  ];

  const mockPage: PropertyPage = {
    content: mockProperties,
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 2,
    empty: false
  };

  beforeEach(async () => {
    mockPropertyService = jasmine.createSpyObj('PropertyService', ['getProperties', 'updatePropertyStatus']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [PropertyListComponent],
      providers: [
        { provide: PropertyService, useValue: mockPropertyService },
        { provide: Router, useValue: mockRouter }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load properties on init', () => {
    mockPropertyService.getProperties.and.returnValue(of(mockPage));

    fixture.detectChanges();

    expect(mockPropertyService.getProperties).toHaveBeenCalledWith(undefined, undefined, 0, 10, 'createdAt,desc');
    expect(component.properties()).toEqual(mockProperties);
    expect(component.totalElements()).toBe(2);
    expect(component.isLoading()).toBeFalse();
  });

  it('should handle loading state', () => {
    mockPropertyService.getProperties.and.returnValue(of(mockPage));

    fixture.detectChanges();

    expect(component.isLoading()).toBeFalse();
  });

  it('should handle error state', () => {
    const errorResponse = new HttpErrorResponse({
      error: { message: 'Failed to load properties.' },
      status: 500
    });
    mockPropertyService.getProperties.and.returnValue(throwError(() => errorResponse));

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('Failed to load properties.');
    expect(component.isLoading()).toBeFalse();
  });

  it('should search properties', () => {
    mockPropertyService.getProperties.and.returnValue(of(mockPage));

    fixture.detectChanges();
    component.searchTerm.set('house');
    component.onSearch();

    expect(mockPropertyService.getProperties).toHaveBeenCalledWith('house', undefined, 0, 10, 'createdAt,desc');
  });

  it('should filter by status', () => {
    mockPropertyService.getProperties.and.returnValue(of(mockPage));

    fixture.detectChanges();
    component.statusFilter.set('ACTIVE');
    component.onStatusFilterChange();

    expect(mockPropertyService.getProperties).toHaveBeenCalledWith(undefined, 'ACTIVE', 0, 10, 'createdAt,desc');
  });

  it('should clear filters', () => {
    mockPropertyService.getProperties.and.returnValue(of(mockPage));

    fixture.detectChanges();
    component.searchTerm.set('house');
    component.statusFilter.set('ACTIVE');
    component.clearFilters();

    expect(component.searchTerm()).toBe('');
    expect(component.statusFilter()).toBe('');
    expect(component.currentPage()).toBe(0);
  });

  it('should format currency correctly', () => {
    const formatted = component.formatCurrency(10000);
    expect(formatted).toContain('₹');
    expect(formatted).toContain('10,000');
  });

  it('should format date correctly', () => {
    const formatted = component.formatDate('2024-01-15T10:00:00');
    expect(formatted).toContain('Jan');
    expect(formatted).toContain('15');
    expect(formatted).toContain('2024');
  });

  it('should get property type label', () => {
    expect(component.getPropertyTypeLabel('HOUSE')).toBe('House');
    expect(component.getPropertyTypeLabel('APARTMENT')).toBe('Apartment');
    expect(component.getPropertyTypeLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should get correct status class', () => {
    expect(component.getStatusClass('ACTIVE')).toBe('status-active');
    expect(component.getStatusClass('INACTIVE')).toBe('status-inactive');
  });
});