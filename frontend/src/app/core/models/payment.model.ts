export type PaymentMethod = 'CASH' | 'UPI' | 'CARD' | 'BANK_TRANSFER' | 'CHEQUE' | 'OTHER';
export type PaymentStatus = 'PENDING' | 'PARTIAL' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export interface Payment {
  id: number;
  rent: RentInfo;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  referenceNumber: string | null;
  status: PaymentStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RentInfo {
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

export type RentStatus = 'PENDING' | 'PARTIAL' | 'PAID' | 'OVERDUE';

export interface PaymentRequest {
  rentId: number;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  referenceNumber?: string;
  notes?: string;
}

export interface PaymentPage {
  content: Payment[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export const PAYMENT_METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'CASH', label: 'Cash' },
  { value: 'UPI', label: 'UPI' },
  { value: 'CARD', label: 'Card' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
  { value: 'CHEQUE', label: 'Cheque' },
  { value: 'OTHER', label: 'Other' }
];

export const PAYMENT_STATUSES: { value: PaymentStatus; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'PARTIAL', label: 'Partial' },
  { value: 'PAID', label: 'Paid' },
  { value: 'OVERDUE', label: 'Overdue' },
  { value: 'CANCELLED', label: 'Cancelled' }
];

export const PAYMENT_METHOD_COLORS: Record<PaymentMethod, string> = {
  CASH: '#6b7280',
  UPI: '#3b82f6',
  CARD: '#8b5cf6',
  BANK_TRANSFER: '#06b6d4',
  CHEQUE: '#f59e0b',
  OTHER: '#64748b'
};

export const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
  PENDING: '#f59e0b',
  PARTIAL: '#3b82f6',
  PAID: '#10b981',
  OVERDUE: '#ef4444',
  CANCELLED: '#6b7280'
};