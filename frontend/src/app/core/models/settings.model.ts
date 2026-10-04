export interface SessionSettings {
  sessionTimeoutMinutes: number;
  autoLogoutOnExpiration: boolean;
  logoutOnBrowserClose: boolean;
  invalidateSessionsOnRestart: boolean;
}

export interface SessionSettingsResponse {
  sessionTimeoutMinutes: number;
  autoLogoutOnExpiration: boolean;
  logoutOnBrowserClose: boolean;
  invalidateSessionsOnRestart: boolean;
  updatedAt?: string;
}