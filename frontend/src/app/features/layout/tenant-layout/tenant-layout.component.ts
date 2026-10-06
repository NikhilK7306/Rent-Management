import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterModule, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '@core/services/auth.service';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-tenant-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, HeaderComponent],
  templateUrl: './tenant-layout.component.html',
  styleUrl: './tenant-layout.component.scss'
})
export class TenantLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  isSidebarCollapsed = false;
  isMobileSidebarOpen = false;
  currentRoute = '';

  navItems = [
    { path: '/tenant/dashboard', label: 'Dashboard', icon: 'dashboard', disabled: false },
    { path: '/tenant/rents', label: 'My Rent', icon: 'rent', disabled: false },
    { path: '/tenant/payments', label: 'Payments', icon: 'payments', disabled: true }
  ];

  get isMobile(): boolean {
    return window.innerWidth < 1024;
  }

  ngOnInit(): void {
    this.router.events.pipe(
      filter((event: RouterEvent): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
      this.isMobileSidebarOpen = false;
    });

    this.currentRoute = this.router.url;
  }

  toggleSidebar(): void {
    if (window.innerWidth < 1024) {
      this.isMobileSidebarOpen = !this.isMobileSidebarOpen;
    } else {
      this.isSidebarCollapsed = !this.isSidebarCollapsed;
    }
  }

  closeMobileSidebar(): void {
    this.isMobileSidebarOpen = false;
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  getCurrentUser() {
    return this.authService.currentUser;
  }

  getIcon(iconName: string): string {
    const icons: Record<string, string> = {
      dashboard: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"></rect><rect x="14" y="3" width="7" height="7" rx="1"></rect><rect x="3" y="14" width="7" height="7" rx="1"></rect><rect x="14" y="14" width="7" height="7" rx="1"></rect></svg>`,
      rent: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>`,
      payments: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>`
    };
    return icons[iconName] || icons['dashboard'];
  }
}