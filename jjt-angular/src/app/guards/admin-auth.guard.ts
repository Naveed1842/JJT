import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { from } from 'rxjs';
import { map } from 'rxjs/operators';
import { FirebaseAuthService } from '../services/firebase-auth.service';

export const adminAuthGuard: CanActivateFn = () => {
  const authService = inject(FirebaseAuthService);
  const router = inject(Router);

  return from(authService.hasAnyRole(['JJT_ADMIN', 'ORG_ADMIN'])).pipe(
    map((allowed) => {
      if (allowed) {
        return true;
      }
      const legacyRole = localStorage.getItem('userRole');
      if (legacyRole === 'JJT_ADMIN' || legacyRole === 'ORG_ADMIN') {
        return true;
      }
      return router.createUrlTree(['/']);
    })
  );
};
