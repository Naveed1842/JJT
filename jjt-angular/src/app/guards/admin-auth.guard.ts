import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminAuthGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  const user = auth.getCurrentUser();
  if (user?.role === 'JJT_ADMIN' || user?.role === 'ORG_ADMIN') {
    return true;
  }
  return router.createUrlTree(['/']);
};
