import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { signal, computed, effect, WritableSignal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { ENVIRONMENT } from '../services/environment.token';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class SessionTimerService {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  // Configuration
  private sessionTimeoutMinutes: WritableSignal<number> = signal(30);
  private refreshThresholdMinutes = 5; // Refresh when ~5 minutes left
  private activityCheckIntervalMs = 1000; // Check every second

  // State
  private lastActivityTimestamp: WritableSignal<number> = signal(0);
  readonly expirationTimestamp = signal<Date | null>(null);
  readonly remainingTime = signal<number>(0);
  private intervalId: number | null = null;
  private warning5MinShown = false;
  private warning1MinShown = false;
  private isRefreshing = false;
  private activityListenersAdded = false;

  // Computed
  readonly expirationTimestamp$ = computed(() => this.expirationTimestamp());
  readonly remainingTime$ = computed(() => this.remainingTime());
  readonly sessionTimeoutMinutes$ = computed(() => this.sessionTimeoutMinutes());

  constructor() {
    if (this.isBrowser) {
      // Initialize last activity to now
      this.lastActivityTimestamp.set(Date.now());

      // Start activity listeners
      this.addActivityListeners();

      // Start the activity check interval
      effect(() => {
        const token = this.authService.accessToken;
        const timeout = this.sessionTimeoutMinutes();
        if (token && timeout > 0) {
          this.startActivityCheck();
        } else {
          this.stopActivityCheck();
        }
      });
    }
  }

  private addActivityListeners(): void {
    if (!this.isBrowser || this.activityListenersAdded) return;

    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
    events.forEach(event => {
      window.addEventListener(event, this.onActivity.bind(this), { passive: true });
    });

    // Handle tab visibility changes
    document.addEventListener('visibilitychange', this.onVisibilityChange.bind(this));

    this.activityListenersAdded = true;
  }

  private removeActivityListeners(): void {
    if (!this.isBrowser || !this.activityListenersAdded) return;

    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
    events.forEach(event => {
      window.removeEventListener(event, this.onActivity.bind(this));
    });

    document.removeEventListener('visibilitychange', this.onVisibilityChange.bind(this));

    this.activityListenersAdded = false;
  }

  private onActivity(): void {
    // Don't update activity for background polling requests
    this.lastActivityTimestamp.set(Date.now());
    this.resetInactivityTimer();
  }

  private onVisibilityChange(): void {
    if (!this.isBrowser) return;

    if (document.visibilityState === 'visible') {
      // Tab became visible - calculate actual elapsed time since last activity
      const elapsed = Date.now() - this.lastActivityTimestamp();
      const timeoutMs = this.sessionTimeoutMinutes() * 60 * 1000;
      
      if (elapsed >= timeoutMs) {
        // Session expired while tab was hidden
        this.handleExpiration();
      } else {
        // Update timer based on actual elapsed time
        this.resetInactivityTimer();
      }
    }
    // When tab becomes hidden, we don't reset the timer - just let it continue
  }

  private startActivityCheck(): void {
    this.stopActivityCheck();
    
    // Initialize expiration timestamp if not set
    if (!this.expirationTimestamp()) {
      this.resetInactivityTimer();
    }

    this.intervalId = window.setInterval(() => {
      this.checkInactivity();
    }, this.activityCheckIntervalMs);
  }

  private stopActivityCheck(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  private resetInactivityTimer(): void {
    const now = Date.now();
    this.lastActivityTimestamp.set(now);
    
    const timeoutMs = this.sessionTimeoutMinutes() * 60 * 1000;
    const exp = new Date(now + timeoutMs);
    this.expirationTimestamp.set(exp);
    
    // Update remaining time
    this.updateRemainingTime();
    
    // Reset warning flags
    this.warning5MinShown = false;
    this.warning1MinShown = false;
  }

  private checkInactivity(): void {
    const now = Date.now();
    const lastActivity = this.lastActivityTimestamp();
    const timeoutMs = this.sessionTimeoutMinutes() * 60 * 1000;
    const elapsed = now - lastActivity;

    // Check if session expired
    if (elapsed >= timeoutMs) {
      this.handleExpiration();
      return;
    }

    // Update remaining time
    this.updateRemainingTime();

    // Check if we should try to refresh the token
    const remainingMs = timeoutMs - elapsed;
    const remainingMinutes = remainingMs / 60000;

    // Try to refresh token when ~5 minutes left and not already refreshing
    if (remainingMinutes <= this.refreshThresholdMinutes && remainingMinutes > 0 && !this.isRefreshing) {
      this.refreshSession();
    }

    // Show warnings
    if (remainingMinutes <= 5 && remainingMinutes > 1 && !this.warning5MinShown) {
      this.warning5MinShown = true;
      this.showWarning('Your session will expire in 5 minutes due to inactivity.');
    } else if (remainingMinutes <= 1 && remainingMinutes > 0 && !this.warning1MinShown) {
      this.warning1MinShown = true;
      this.showWarning('Your session will expire in 1 minute due to inactivity.');
    }
  }

  private updateRemainingTime(): void {
    const now = Date.now();
    const exp = this.expirationTimestamp();
    if (!exp) {
      this.remainingTime.set(0);
      return;
    }

    const remaining = Math.max(0, Math.floor((exp.getTime() - now) / 1000));
    this.remainingTime.set(remaining);
  }

  private async refreshSession(): Promise<void> {
    if (this.isRefreshing || !this.authService.isAuthenticated) return;

    this.isRefreshing = true;
    
    try {
      const response = await this.http.post<{ accessToken: string; tokenType: string; user: any }>(
        `${this.env.apiBaseUrl}/auth/refresh`,
        {},
        { withCredentials: false }
      ).toPromise();

      if (response?.accessToken) {
        // Update auth state with new token
        this.authService.updateToken(response.accessToken, response.user);
        console.debug('Session refreshed successfully');
        
        // Reset timer with new token
        this.resetInactivityTimer();
      }
    } catch (error) {
      console.error('Failed to refresh session:', error);
      // If refresh fails, let the normal expiration handle it
    } finally {
      this.isRefreshing = false;
    }
  }

  private showWarning(message: string): void {
    if (this.isBrowser && 'Notification' in window && Notification.permission === 'granted') {
      new Notification('Session Expiring Soon', { body: message });
    }
    console.warn('[SessionTimer]', message);
  }

  private handleExpiration(): void {
    this.stopActivityCheck();
    this.removeActivityListeners();
    this.authService.logout();
    if (this.isBrowser) {
      alert('Your session has expired due to inactivity. Please log in again.');
      this.router.navigate(['/login']);
    }
  }

  stopTimer(): void {
    this.stopActivityCheck();
    this.removeActivityListeners();
    this.remainingTime.set(0);
    this.expirationTimestamp.set(null);
    this.warning5MinShown = false;
    this.warning1MinShown = false;
    this.isRefreshing = false;
  }

  updateSessionTimeout(minutes: number): void {
    this.sessionTimeoutMinutes.set(minutes);
    if (this.authService.isAuthenticated) {
      this.resetInactivityTimer();
    }
  }

  // Cleanup on destroy
  ngOnDestroy(): void {
    this.stopTimer();
  }
}