export type PropertyType = 'HOUSE' | 'APARTMENT' | 'ROOM' | 'SHOP' | 'OFFICE' | 'OTHER';
export type PropertyStatus = 'ACTIVE' | 'INACTIVE';

export interface PropertyTenantInfo {
  id: number;
  fullName: string;
  mobileNumber: string;
}

export interface Property {
  id: number;
  propertyName: string;
  propertyCode: string;
  propertyType: PropertyType;
  address: string;
  description: string | null;
  monthlyRent: number;
  status: PropertyStatus;
  createdAt: string;
  updatedAt: string;
  tenantName: string;
  tenant: PropertyTenantInfo | null;
}

export interface PropertyRequest {
  propertyName: string;
  propertyCode: string;
  propertyType: PropertyType;
  address: string;
  description?: string;
  monthlyRent: number;
}

export interface PropertyStatusRequest {
  status: PropertyStatus;
}

export interface PropertyPage {
  content: Property[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export const PROPERTY_TYPES: { value: PropertyType; label: string }[] = [
  { value: 'HOUSE', label: 'House' },
  { value: 'APARTMENT', label: 'Apartment' },
  { value: 'ROOM', label: 'Room' },
  { value: 'SHOP', label: 'Shop' },
  { value: 'OFFICE', label: 'Office' },
  { value: 'OTHER', label: 'Other' }
];

export const PROPERTY_STATUSES: { value: PropertyStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' }
];