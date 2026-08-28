import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [() => import('./core/guards/guest.guard').then(m => m.guestGuard)]
  },
  {
    path: '',
    loadComponent: () => import('./features/layout/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [() => import('./core/guards/auth.guard').then(m => m.authGuard)],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'properties',
        loadComponent: () => import('./features/properties/property-list.component').then(m => m.PropertyListComponent)
      },
      {
        path: 'tenants',
        loadComponent: () => import('./features/tenants/tenant-list.component').then(m => m.TenantListComponent)
      },
      {
        path: 'rents',
        loadComponent: () => import('./features/rents/rent-list.component').then(m => m.RentListComponent)
      },
      {
        path: 'payments',
        loadComponent: () => import('./features/payments/payment-list.component').then(m => m.PaymentListComponent)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/layout/coming-soon/coming-soon.component').then(m => m.ComingSoonComponent),
        data: { title: 'Reports', feature: 'Reports & Analytics' }
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/layout/coming-soon/coming-soon.component').then(m => m.ComingSoonComponent),
        data: { title: 'Settings', feature: 'System Settings' }
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];