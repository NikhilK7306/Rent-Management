import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const tenantGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated && authService.currentUser?.role === 'TENANT') {
    return true;
  }

  // If authenticated but not tenant, redirect to admin dashboard
  if (authService.isAuthenticated && authService.currentUser?.role === 'ADMIN') {
    router.navigate(['/dashboard']);
    return false;
  }

  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};