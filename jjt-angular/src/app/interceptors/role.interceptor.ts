import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { FirebaseAuthService } from '../services/firebase-auth.service';

export const roleInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/public/')) {
    return next(req);
  }
  const authService = inject(FirebaseAuthService);

  return from(authService.getIdToken()).pipe(
    switchMap((token) => {
      if (token) {
        return next(req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        }));
      }

      // Legacy fallback for migration period.
      if (req.headers.has('X-ROLE')) {
        const existingRole = req.headers.get('X-ROLE');
        if (existingRole === 'SPONSOR' && !req.headers.has('X-SPONSOR-ID')) {
          const withSponsorId = req.clone({
            setHeaders: { 'X-SPONSOR-ID': environment.sponsorId }
          });
          return next(withSponsorId);
        }
        return next(req);
      }

      const role = localStorage.getItem('userRole') || 'ORG_ADMIN';
      const headers: Record<string, string> = { 'X-ROLE': role };

      if (role === 'SPONSOR') {
        headers['X-SPONSOR-ID'] = environment.sponsorId;
      }

      return next(req.clone({ setHeaders: headers }));
    })
  );
};
