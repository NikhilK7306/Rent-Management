import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PropertyFormComponent } from './property-form.component';
import { PropertyService } from '@core/services/property.service';
import { Property, PropertyRequest, PropertyType } from '@core/models/property.model';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

describe('PropertyFormComponent', () => {
  let component: PropertyFormComponent;
  let fixture: ComponentFixture<PropertyFormComponent>;
  let mockPropertyService: jasmine.SpyObj<PropertyService>;

  const mockProperty: Property = {
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
  };

  beforeEach(async () => {
    mockPropertyService = jasmine.createSpyObj('PropertyService', ['createProperty', 'updateProperty']);

    await TestBed.configureTestingModule({
      imports: [PropertyFormComponent, ReactiveFormsModule],
      providers: [
        { provide: PropertyService, useValue: mockPropertyService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyFormComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('isOpen', true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values for new property', () => {
    expect(component.form.value.propertyName).toBe('');
    expect(component.form.value.propertyCode).toBe('');
    expect(component.form.value.propertyType).toBe('HOUSE');
    expect(component.form.value.address).toBe('');
    expect(component.form.value.description).toBe('');
    expect(component.form.value.monthlyRent).toBe(0);
  });

  it('should populate form when editing existing property', () => {
    fixture.componentRef.setInput('property', mockProperty);
    fixture.detectChanges();

    expect(component.form.value.propertyName).toBe('House A');
    expect(component.form.value.propertyCode).toBe('PROP-001');
    expect(component.form.value.propertyType).toBe('HOUSE');
    expect(component.form.value.address).toBe('Thrissur, Kerala');
    expect(component.form.value.description).toBe('Two bedroom rental house');
    expect(component.form.value.monthlyRent).toBe(10000);
  });

  it('should validate required fields', () => {
    expect(component.form.valid).toBeFalse();

    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });

    expect(component.form.valid).toBeTrue();
  });

  it('should invalidate empty property name', () => {
    component.form.patchValue({
      propertyName: '',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });

    expect(component.form.get('propertyName')?.hasError('required')).toBeTrue();
  });

  it('should invalidate empty property code', () => {
    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: '',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });

    expect(component.form.get('propertyCode')?.hasError('required')).toBeTrue();
  });

  it('should invalidate zero monthly rent', () => {
    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 0
    });

    expect(component.form.get('monthlyRent')?.hasError('min')).toBeTrue();
  });

  it('should invalidate negative monthly rent', () => {
    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: -100
    });

    expect(component.form.get('monthlyRent')?.hasError('min')).toBeTrue();
  });

  it('should call createProperty service for new property', () => {
    const request: PropertyRequest = {
      propertyName: 'New Property',
      propertyCode: 'PROP-NEW',
      propertyType: 'HOUSE',
      address: 'New Address',
      description: 'Description',
      monthlyRent: 12000
    };

    mockPropertyService.createProperty.and.returnValue(of({ ...request, id: 2 } as Property));

    component.form.patchValue(request);
    component.onSubmit();

    expect(mockPropertyService.createProperty).toHaveBeenCalledWith(request);
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should call updateProperty service for existing property', () => {
    fixture.componentRef.setInput('property', mockProperty);
    fixture.detectChanges();

    const request: PropertyRequest = {
      propertyName: 'Updated Property',
      propertyCode: 'PROP-001',
      propertyType: 'APARTMENT',
      address: 'Updated Address',
      description: 'Updated Description',
      monthlyRent: 15000
    };

    mockPropertyService.updateProperty.and.returnValue(of({ ...mockProperty, ...request } as Property));

    component.form.patchValue(request);
    component.onSubmit();

    expect(mockPropertyService.updateProperty).toHaveBeenCalledWith(1, request);
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should handle create property error', () => {
    const errorResponse = new HttpErrorResponse({
      error: { message: 'Property code already exists' },
      status: 409
    });
    mockPropertyService.createProperty.and.returnValue(throwError(() => errorResponse));

    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });
    component.onSubmit();

    expect(component.errorMessage()).toBe('Property code already exists');
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should handle validation errors from backend', () => {
    const errorResponse = new HttpErrorResponse({
      error: {
        validationErrors: {
          propertyCode: 'Property code already exists',
          monthlyRent: 'Monthly rent must be greater than zero'
        }
      },
      status: 400
    });
    mockPropertyService.createProperty.and.returnValue(throwError(() => errorResponse));

    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });
    component.onSubmit();

    expect(component.form.get('propertyCode')?.hasError('serverError')).toBeTrue();
    expect(component.form.get('monthlyRent')?.hasError('serverError')).toBeTrue();
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should emit close event', () => {
    spyOn(component.close, 'emit');
    component.onClose();
    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should reset form on close', () => {
    component.form.patchValue({
      propertyName: 'Test Property',
      propertyCode: 'PROP-001',
      propertyType: 'HOUSE',
      address: 'Test Address',
      monthlyRent: 10000
    });

    component.onClose();

    expect(component.form.value.propertyName).toBe('');
    expect(component.form.value.propertyCode).toBe('');
    expect(component.form.value.monthlyRent).toBe(0);
  });

  it('should get correct error messages', () => {
    component.form.get('propertyName')?.markAsTouched();
    component.form.get('propertyName')?.setErrors({ required: true });

    expect(component.getError('propertyName')).toBe('Property Name is required');
  });

  it('should identify invalid fields', () => {
    component.form.get('propertyName')?.markAsTouched();
    component.form.get('propertyName')?.setErrors({ required: true });

    expect(component.isFieldInvalid('propertyName')).toBeTrue();
    expect(component.isFieldInvalid('propertyCode')).toBeFalse();
  });
});