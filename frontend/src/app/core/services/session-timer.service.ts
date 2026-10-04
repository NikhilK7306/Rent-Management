import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { signal, computed, effect, WritableSignal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class SessionTimerService {
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private sessionTimeoutMinutes: WritableSignal<number> = signal(30);
  expirationTimestamp = signal<Date | null>(null);
  remainingTime = signal<number>(0);
  private intervalId: number | null = null;
  private warning5MinShown = false;
  private warning1MinShown = false;

  constructor() {
    if (this.isBrowser) {
      effect(() => {
        const token = this.authService.accessToken;
        const timeout = this.sessionTimeoutMinutes();
        if (token && timeout > 0) {
          this.startTimer();
        } else {
          this.stopTimer();
        }
      });
    }
  }

  startTimer(): void {
    this.stopTimer();
    const now = new Date();
    const exp = new Date(now.getTime() + this.sessionTimeoutMinutes() * 60 * 1000);
    this.expirationTimestamp.set(exp);
    this.remainingTime.set(this.sessionTimeoutMinutes() * 60);
    this.warning5MinShown = false;
    this.warning1MinShown = false;

    this.intervalId = window.setInterval(() => {
      this.tick();
    }, 1000);
  }

  private tick(): void {
    const now = new Date();
    const exp = this.expirationTimestamp();
    if (!exp) {
      this.stopTimer();
      return;
    }

    const remaining = Math.max(0, Math.floor((exp.getTime() - now.getTime()) / 1000));
    this.remainingTime.set(remaining);

    if (remaining <= 300 && remaining > 60 && !this.warning5MinShown) {
      this.warning5MinShown = true;
      this.showWarning('Your session will expire in 5 minutes.');
    } else if (remaining <= 60 && remaining > 0 && !this.warning1MinShown) {
      this.warning1MinShown = true;
      this.showWarning('Your session will expire in 1 minute.');
    } else if (remaining === 0) {
      this.handleExpiration();
    }
  }

  private showWarning(message: string): void {
    if (this.isBrowser && 'Notification' in window && Notification.permission === 'granted') {
      new Notification('Session Expiring Soon', { body: message });
    }
    console.warn('[SessionTimer]', message);
  }

  private handleExpiration(): void {
    this.stopTimer();
    this.authService.logout();
    if (this.isBrowser) {
      alert('Your session has expired. Please log in again.');
      window.location.href = '/login';
    }
  }

  stopTimer(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.remainingTime.set(0);
    this.expirationTimestamp.set(null);
    this.warning5MinShown = false;
    this.warning1MinShown = false;
  }

  updateSessionTimeout(minutes: number): void {
    this.sessionTimeoutMinutes.set(minutes);
    if (this.authService.isAuthenticated) {
      this.startTimer();
    }
  }

  extendSession(additionalMinutes: number): void {
    if (this.authService.isAuthenticated) {
      const now = new Date();
      const exp = new Date(now.getTime() + (this.remainingTime() + additionalMinutes * 60) * 1000);
      this.expirationTimestamp.set(exp);
      this.remainingTime.set(this.remainingTime() + additionalMinutes * 60);
    }
  }
}