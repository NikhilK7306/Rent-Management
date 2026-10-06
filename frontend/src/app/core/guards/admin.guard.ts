import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated && authService.currentUser?.role === 'ADMIN') {
    return true;
  }

  // If authenticated but not admin, redirect to tenant dashboard
  if (authService.isAuthenticated && authService.currentUser?.role === 'TENANT') {
    router.navigate(['/tenant/dashboard']);
    return false;
  }

  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};