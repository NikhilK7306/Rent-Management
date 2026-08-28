export type TenantStatus = 'ACTIVE' | 'INACTIVE';

export interface Tenant {
  id: number;
  fullName: string;
  mobileNumber: string;
  email: string | null;
  address: string | null;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
  property: TenantPropertyInfo | null;
}

export interface TenantPropertyInfo {
  id: number;
  propertyCode: string;
  propertyName: string;
  propertyType: string;
}

export interface TenantRequest {
  fullName: string;
  mobileNumber: string;
  email?: string;
  address?: string;
}

export interface TenantStatusRequest {
  status: TenantStatus;
}

export interface TenantPropertyRequest {
  propertyId: number;
}

export interface TenantPage {
  content: Tenant[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export const TENANT_STATUSES: { value: TenantStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' }
];