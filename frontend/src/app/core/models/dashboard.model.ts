export interface DashboardResponse {
  message: string;
  adminName: string;
  role: string;
  propertyCount: number;
  systemStatus: { status: string };
  databaseStatus: { status: string };
  backendStatus: { status: string };
}