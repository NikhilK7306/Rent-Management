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

export type TenantStatus = 'ACTIVE' | 'INACTIVE';

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

// Tenant Portal Types
export interface TenantMeResponse {
  id: number;
  fullName: string;
  mobileNumber: string;
  email: string | null;
  address: string | null;
  status: 'ACTIVE' | 'INACTIVE';
  property: TenantPortalPropertyInfo | null;
}

export interface TenantPortalPropertyInfo {
  id: number;
  propertyCode: string;
  propertyName: string;
  propertyType: string;
  address: string | null;
  monthlyRent: string | null;
}

export interface TenantDashboardResponse {
  tenantName: string;
  mobileNumber: string;
  email: string | null;
  property: TenantDashboardPropertyInfo | null;
  rentSummary: TenantRentSummary | null;
}

export interface TenantDashboardPropertyInfo {
  id: number;
  propertyCode: string;
  propertyName: string;
  propertyType: string;
  address: string | null;
  monthlyRent: string | null;
}

export interface TenantRentSummary {
  currentMonthRent: string;
  paidAmount: string;
  outstandingAmount: string;
  status: string;
  rentMonth: number;
  rentYear: number;
}

export interface TenantRentResponse {
  id: number;
  tenantId: number;
  propertyId: number;
  rentMonth: number;
  rentYear: number;
  monthlyRent: string;
  dueDate: string;
  status: string;
  paidAmount: string | null;
  paidDate: string | null;
  property: {
    id: number;
    propertyCode: string;
    propertyName: string;
  };
}

export interface TenantRentPage {
  content: TenantRentResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}