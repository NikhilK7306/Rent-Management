export interface NotificationResponse {
  id: number;
  type: string;
  title: string;
  message: string;
  priority: string;
  isRead: boolean;
  tenantId?: number;
  tenantName?: string;
  propertyId?: number;
  propertyName?: string;
  rentId?: number;
  paymentId?: number;
  referenceKey: string;
  createdAt: string;
  updatedAt: string;
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