import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { from, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

// Paths that never need an Authorization header and must never trigger a refresh loop.
const SKIP_AUTH_PATTERNS = ['/api/auth/login', '/api/auth/refresh', '/api/public/'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (SKIP_AUTH_PATTERNS.some(p => req.url.includes(p))) {
    return next(req);
  }

  const token = authService.getAccessToken();
  const outgoing = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(outgoing).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401) return throwError(() => err);

      // Access token expired — attempt one silent refresh.
      return from(authService.refreshAccessToken()).pipe(
        switchMap(newToken =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } }))
        ),
        catchError(refreshErr => {
          // Refresh failed (revoked, expired, network error) — clear local state and go to
          // login, preserving where the user was so they land back there after signing in.
          authService.clearSession();
          const currentUrl = router.routerState.snapshot.url;
          router.navigate(['/login'],
            currentUrl && !currentUrl.startsWith('/login')
              ? { queryParams: { returnUrl: currentUrl } }
              : undefined);
          return throwError(() => refreshErr);
        })
      );
    })
  );
};
