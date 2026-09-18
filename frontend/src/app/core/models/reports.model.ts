export interface RentReportRequest {
  month?: number;
  year?: number;
  fromDate?: string;
  toDate?: string;
  tenantId?: number;
  propertyId?: number;
  status?: string;
  page?: number;
  size?: number;
}

export interface RentReportResponse {
  totalRecords: number;
  totalRent: number;
  totalPaid: number;
  totalOutstanding: number;
  paidCount: number;
  partialCount: number;
  pendingCount: number;
  items: RentReportItem[];
}

export interface RentReportItem {
  rentId: number;
  tenantName: string;
  tenantMobile: string;
  propertyName: string;
  propertyCode: string;
  month: number;
  year: number;
  period: string;
  rentAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  status: string;
  dueDate: string;
}

export interface PaymentReportRequest {
  month?: number;
  year?: number;
  fromDate?: string;
  toDate?: string;
  tenantId?: number;
  propertyId?: number;
  status?: string;
  paymentMethod?: string;
  page?: number;
  size?: number;
}

export interface PaymentReportResponse {
  totalRecords: number;
  totalAmount: number;
  completedCount: number;
  partialCount: number;
  paymentMethodBreakdown: PaymentMethodBreakdown[];
  items: PaymentReportItem[];
}

export interface PaymentMethodBreakdown {
  paymentMethod: string;
  totalAmount: number;
  count: number;
}

export interface PaymentReportItem {
  paymentId: number;
  tenantName: string;
  propertyName: string;
  propertyCode: string;
  rentMonth: number;
  rentYear: number;
  rentPeriod: string;
  paymentDate: string;
  amount: number;
  paymentMethod: string;
  status: string;
  referenceNumber: string;
}

export interface OutstandingReportRequest {
  month?: number;
  year?: number;
  fromDate?: string;
  toDate?: string;
  tenantId?: number;
  propertyId?: number;
  page?: number;
  size?: number;
}

export interface OutstandingReportResponse {
  totalOutstanding: number;
  totalTenants: number;
  totalRecords: number;
  items: OutstandingReportItem[];
}

export interface OutstandingReportItem {
  rentId: number;
  tenantName: string;
  tenantMobile: string;
  propertyName: string;
  propertyCode: string;
  month: number;
  year: number;
  period: string;
  rentAmount: number;
  totalPaid: number;
  outstandingAmount: number;
  status: string;
}

export interface TenantReportResponse {
  tenantName: string;
  tenantMobile: string;
  tenantEmail: string;
  propertyName: string;
  propertyCode: string;
  totalRentGenerated: number;
  totalPaid: number;
  totalOutstanding: number;
  paidRents: number;
  pendingRents: number;
  partialRents: number;
  rentHistory: TenantRentHistoryItem[];
}

export interface TenantRentHistoryItem {
  rentId: number;
  month: number;
  year: number;
  period: string;
  rentAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  status: string;
  dueDate: string;
  payments: PaymentSummaryItem[];
}

export interface PaymentSummaryItem {
  paymentId: number;
  paymentDate: string;
  amount: number;
  paymentMethod: string;
  status: string;
}

export interface PropertyReportResponse {
  propertyName: string;
  propertyCode: string;
  propertyType: string;
  address: string;
  monthlyRent: number;
  currentTenantName: string;
  currentTenantMobile: string;
  isOccupied: boolean;
  totalRentGenerated: number;
  totalCollected: number;
  totalOutstanding: number;
  rentHistory: PropertyRentHistoryItem[];
}

export interface PropertyRentHistoryItem {
  rentId: number;
  tenantName: string;
  tenantMobile: string;
  month: number;
  year: number;
  period: string;
  rentAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  status: string;
  dueDate: string;
}

export interface RentStatusReportResponse {
  paidCount: number;
  partialCount: number;
  pendingCount: number;
  paidAmount: number;
  partialAmount: number;
  pendingAmount: number;
}