import { Injectable, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toasts = signal<Toast[]>([]);
  private idCounter = 0;

  readonly toasts$ = computed(() => this.toasts());

  show(toast: Omit<Toast, 'id'>): void {
    const id = ++this.idCounter;
    const newToast: Toast = { ...toast, id, duration: toast.duration ?? 5000 };
    this.toasts.update(toasts => [...toasts, newToast]);

    if (newToast.duration && newToast.duration > 0) {
      setTimeout(() => this.remove(id), newToast.duration);
    }
  }

  success(title: string, message?: string): void {
    this.show({ type: 'success', title, message });
  }

  error(title: string, message?: string): void {
    this.show({ type: 'error', title, message, duration: 8000 });
  }

  warning(title: string, message?: string): void {
    this.show({ type: 'warning', title, message });
  }

  info(title: string, message?: string): void {
    this.show({ type: 'info', title, message });
  }

  remove(id: number): void {
    this.toasts.update(toasts => toasts.filter(t => t.id !== id));
  }

  clear(): void {
    this.toasts.set([]);
  }
}

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" aria-live="polite" aria-atomic="true">
      @for (toast of toastService.toasts$(); track toast.id) {
        <div class="toast" [class]="'toast-' + toast.type" role="alert" aria-live="assertive" aria-atomic="true">
          <div class="toast-header">
            <span class="toast-title">{{ toast.title }}</span>
            <button type="button" class="toast-close" (click)="toastService.remove(toast.id)" aria-label="Close">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          @if (toast.message) {
            <div class="toast-body">{{ toast.message }}</div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-width: 400px;
    }

    .toast {
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-left: 4px solid;
      overflow: hidden;
      animation: slideIn 0.3s ease-out;
    }

    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .toast-success { border-left-color: #10b981; }
    .toast-error { border-left-color: #ef4444; }
    .toast-warning { border-left-color: #f59e0b; }
    .toast-info { border-left-color: #3b82f6; }

    .toast-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
    }

    .toast-title {
      font-weight: 600;
      font-size: 0.875rem;
      color: #1f2937;
    }

    .toast-close {
      background: none;
      border: none;
      cursor: pointer;
      color: #9ca3af;
      padding: 0.25rem;
      border-radius: 4px;
      transition: color 0.2s, background 0.2s;
    }

    .toast-close:hover {
      color: #374151;
      background: #f3f4f6;
    }

    .toast-body {
      padding: 0 1rem 0.75rem;
      font-size: 0.875rem;
      color: #4b5563;
    }

    .toast-success .toast-title { color: #065f46; }
    .toast-error .toast-title { color: #991b1b; }
    .toast-warning .toast-title { color: #92400e; }
    .toast-info .toast-title { color: #1e40af; }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}