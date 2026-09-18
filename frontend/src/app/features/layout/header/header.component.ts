import { Component, Input, Output, EventEmitter, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '@core/services/notification.service';
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

  @Input() isSidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  breadcrumb = [
    { label: 'Dashboard', path: '/dashboard' }
  ];

  showNotificationCenter = signal(false);
  unreadCount = signal(0);

  ngOnInit(): void {
    this.loadUnreadCount();
    // Refresh unread count every 30 seconds
    setInterval(() => this.loadUnreadCount(), 30000);
  }

  loadUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => {} // Silently ignore errors
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
}