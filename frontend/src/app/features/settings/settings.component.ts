import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '@core/services/auth.service';
import { SettingsService } from '@core/services/settings.service';
import { SessionTimerService } from '@core/services/session-timer.service';
import { SessionSettings, SessionSettingsResponse } from '@core/models/settings.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  private authService = inject(AuthService);
  private settingsService = inject(SettingsService);
  private sessionTimerService = inject(SessionTimerService);

  sessionTimeout = signal<number>(30);
  autoLogout = signal<boolean>(true);
  logoutOnClose = signal<boolean>(true);
  invalidateOnRestart = signal<boolean>(true);

  isLoading = signal(false);
  saving = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  currentUser = computed(() => this.authService.currentUser);
  sessionExpiration = computed(() => this.sessionTimerService.expirationTimestamp());
  sessionRemaining = computed(() => this.sessionTimerService.remainingTime());

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.settingsService.getSessionSettings().subscribe({
      next: (response: SessionSettingsResponse) => {
        this.sessionTimeout.set(response.sessionTimeoutMinutes);
        this.autoLogout.set(response.autoLogoutOnExpiration);
        this.logoutOnClose.set(response.logoutOnBrowserClose);
        this.invalidateOnRestart.set(response.invalidateSessionsOnRestart);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to load session settings.');
      }
    });
  }

  saveSettings(): void {
    this.saving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const settings: SessionSettings = {
      sessionTimeoutMinutes: this.sessionTimeout(),
      autoLogoutOnExpiration: this.autoLogout(),
      logoutOnBrowserClose: this.logoutOnClose(),
      invalidateSessionsOnRestart: this.invalidateOnRestart()
    };

    this.settingsService.updateSessionSettings(settings).subscribe({
      next: (response: SessionSettingsResponse) => {
        this.sessionTimeout.set(response.sessionTimeoutMinutes);
        this.sessionTimerService.updateSessionTimeout(response.sessionTimeoutMinutes);
        this.saving.set(false);
        this.successMessage.set('Session settings updated successfully.');
        setTimeout(() => this.successMessage.set(''), 5000);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to save settings. Session timeout must be between 5 minutes and 24 hours.');
      }
    });
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  getSessionStarted(): string {
    const exp = this.sessionExpiration();
    if (!exp) return '—';
    const started = new Date(exp.getTime() - this.sessionTimeout() * 60 * 1000);
    return started.toLocaleString();
  }
}