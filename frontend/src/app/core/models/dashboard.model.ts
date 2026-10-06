export interface DashboardResponse {
  message: string;
  adminName: string;
  role: string;
  propertyCount: number;
  systemStatus: { status: string };
  databaseStatus: { status: string };
  backendStatus: { status: string };
}

export interface DashboardSummaryResponse {
  message: string;
  adminName: string;
  role: string;
  totalProperties: number;
  occupiedProperties: number;
  vacantProperties: number;
  activeTenants: number;
  totalRentDue: number;
  totalRentCollected: number;
  totalOutstanding: number;
  pendingRents: number;
  overdueRents: number;
  upcomingRents: number;
  partialRents: number;
  rentSummary: RentSummary;
  paymentSummary: PaymentSummary;
  monthlyRentOverview: MonthlyRentOverview[];
  notificationSummary?: NotificationSummaryResponse;
}

export interface NotificationSummaryResponse {
  totalUnread: number;
  overdueCount: number;
  pendingCount: number;
  partialCount: number;
  upcomingCount: number;
  paidCount: number;
  totalOutstanding: number;
}

export interface RentSummary {
  totalRent: number;
  paidRent: number;
  partiallyPaidRent: number;
  pendingRent: number;
  outstandingAmount: number;
}

export interface PaymentSummary {
  totalPayments: number;
  totalAmountCollected: number;
  completedPayments: number;
  partialPayments: number;
  paymentMethodBreakdown: PaymentMethodBreakdown[];
}

export interface PaymentMethodBreakdown {
  paymentMethod: string;
  totalAmount: number;
  count: number;
}

export interface MonthlyRentOverview {
  month: number;
  year: number;
  monthName: string;
  rentDue: number;
  collected: number;
  outstanding: number;
  paidCount: number;
  partialCount: number;
  pendingCount: number;
  overdueCount: number;
}