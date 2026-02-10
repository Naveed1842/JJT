import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip authentication for public endpoints
  if (req.url.includes('/api/public/') || req.url.includes('/api/auth/')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    // Add JWT token to Authorization header
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  // If no token, proceed without authentication (will be handled by backend)
  return next(req);
};
