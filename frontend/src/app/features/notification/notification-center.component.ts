import { Component, OnInit, inject, signal, computed, Output, EventEmitter, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '@core/services/notification.service';
import { NotificationResponse } from '@core/models/notification.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-notification-center',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notification-center.component.html',
  styleUrl: './notification-center.component.scss'
})
export class NotificationCenterComponent implements OnInit {
  private notificationService = inject(NotificationService);

  @Output() closePanel = new EventEmitter<void>();

  notifications = signal<NotificationResponse[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');
  currentPage = signal(0);
  pageSize = 20;
  totalElements = signal(0);
  showOnlyUnread = signal(false);

  totalPages = computed(() => Math.ceil(this.totalElements() / this.pageSize));

  ngOnInit(): void {
    this.loadNotifications();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.notification-center') && !target.closest('.notification-bell')) {
      this.closePanel.emit();
    }
  }

  loadNotifications(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const loadFn = this.showOnlyUnread() ? 'getUnreadNotifications' : 'getAllNotifications';
    this.notificationService[loadFn](this.currentPage(), this.pageSize).subscribe({
      next: (data: { content: NotificationResponse[], totalElements: number }) => {
        this.notifications.set(data.content);
        this.totalElements.set(data.totalElements);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to backend.');
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to load notifications.');
        }
      }
    });
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadNotifications();
    }
  }

  toggleFilter(): void {
    this.showOnlyUnread.update(v => !v);
    this.currentPage.set(0);
    this.loadNotifications();
  }

  markAsRead(id: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(id).subscribe({
      next: (updated) => {
        this.notifications.update(list => list.map(n => n.id === id ? updated : n));
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, isRead: true })));
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getPriorityClass(priority: string): string {
    switch (priority.toUpperCase()) {
      case 'HIGH': return 'priority-high';
      case 'MEDIUM': return 'priority-medium';
      case 'LOW': return 'priority-low';
      default: return 'priority-info';
    }
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      RENT_DUE: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>',
      RENT_PENDING: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="12" y1="16" x2="12" y2="12"></line></svg>',
      RENT_OVERDUE: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>',
      RENT_PARTIAL: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line></svg>',
      RENT_PAID: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>',
      PAYMENT_RECEIVED: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>',
      OUTSTANDING_RENT: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>',
      SYSTEM: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line></svg>'
    };
    return icons[type] || icons['SYSTEM'];
  }

  retry(): void {
    this.loadNotifications();
  }
}