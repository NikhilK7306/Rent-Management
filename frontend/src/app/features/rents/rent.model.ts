export type RentStatus = 'PENDING' | 'PARTIAL' | 'PAID' | 'OVERDUE';

export interface Rent {
  id: number;
  tenant: TenantInfo;
  property: PropertyInfo;
  rentMonth: number;
  rentYear: number;
  monthlyRent: number;
  dueDate: string;
  status: RentStatus;
  paidDate: string | null;
  paidAmount: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface TenantInfo {
  id: number;
  fullName: string;
  mobileNumber: string;
}

export interface PropertyInfo {
  id: number;
  propertyCode: string;
  propertyName: string;
  propertyType: string;
  monthlyRent: number;
}

export interface RentRequest {
  tenantId: number;
  propertyId: number;
  rentMonth: number;
  rentYear: number;
  monthlyRent: number;
  dueDate?: string;
}

export interface RentStatusRequest {
  status: RentStatus;
}

export interface BulkRentRequest {
  tenantId: number;
  propertyId: number;
  startMonth: number;
  startYear: number;
  endMonth: number;
  endYear: number;
  monthlyRent: number;
  dueDate?: string;
  initialStatus?: RentStatus;
}

export interface BulkRentResult {
  createdCount: number;
  skippedCount: number;
  skippedMonths: SkippedMonth[];
  totalAmount: number;
}

export interface SkippedMonth {
  month: number;
  year: number;
  reason: string;
}

export interface TotalOutstandingRequest {
  tenantId: number;
  propertyId: number;
  asOfDate: string;
  totalOutstandingAmount: number;
  numberOfMonths?: number;
  notes?: string;
}

export interface TotalOutstandingResult {
  createdCount: number;
  monthlyAmount: number;
  totalAmount: number;
  isConsolidated: boolean;
}

export interface RentPage {
  content: Rent[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export const RENT_STATUSES: { value: RentStatus; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'PARTIAL', label: 'Partial' },
  { value: 'PAID', label: 'Paid' },
  { value: 'OVERDUE', label: 'Overdue' }
];