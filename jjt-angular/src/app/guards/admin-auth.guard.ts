import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminAuthGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Session restore runs in the background at bootstrap; wait for it to settle
  // so a hard refresh on /admin doesn't bounce to /login before the token returns.
  return auth.ready.then(() => {
    if (!auth.isAuthenticated()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    const user = auth.getCurrentUser();
    if (user?.role === 'JJT_ADMIN' || user?.role === 'ORG_ADMIN') {
      return true;
    }
    return router.createUrlTree(['/']);
  });
};
