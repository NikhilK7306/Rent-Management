import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { SessionTimerService } from '../services/session-timer.service';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const sessionTimerService = inject(SessionTimerService);
  const router = inject(Router);

  const token = authService.accessToken;
  const isAuthEndpoint = req.url.includes('/api/auth/');
  const isHealthEndpoint = req.url.includes('/api/health');

  let authReq = req;
  if (token && !isAuthEndpoint && !isHealthEndpoint) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthEndpoint && !isHealthEndpoint) {
        sessionTimerService.stopTimer();
        authService.logout();
        router.navigate(['/login'], { replaceUrl: true });
      }
      return throwError(() => error);
    })
  );
};