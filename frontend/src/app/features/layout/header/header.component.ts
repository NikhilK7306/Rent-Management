import { Component, Input, Output, EventEmitter, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '@core/services/notification.service';
import { SessionTimerService } from '@core/services/session-timer.service';
import { NotificationCenterComponent } from '../../notification/notification-center.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationCenterComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private sessionTimerService = inject(SessionTimerService);

  @Input() isSidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  breadcrumb = [
    { label: 'Dashboard', path: '/dashboard' }
  ];

  showNotificationCenter = signal(false);
  unreadCount = signal(0);

  remainingTime = computed(() => this.sessionTimerService.remainingTime());
  expirationTimestamp = computed(() => this.sessionTimerService.expirationTimestamp());

  ngOnInit(): void {
    this.loadUnreadCount();
    setInterval(() => this.loadUnreadCount(), 30000);
  }

  loadUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => {}
    });
  }

  toggleNotificationCenter(): void {
    this.showNotificationCenter.update(v => !v);
    if (this.showNotificationCenter()) {
      this.loadUnreadCount();
    }
  }

  closeNotificationCenter(): void {
    this.showNotificationCenter.set(false);
  }

  get unreadBadge(): string {
    const count = this.unreadCount();
    return count > 99 ? '99+' : count.toString();
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
}