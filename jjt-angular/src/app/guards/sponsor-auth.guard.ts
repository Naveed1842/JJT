import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const sponsorAuthGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Session restore runs in the background at bootstrap; wait for it to settle
  // so a hard refresh on /sponsor/portal doesn't bounce to /login prematurely.
  return auth.ready.then(() => {
    if (!auth.isAuthenticated()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    const user = auth.getCurrentUser();
    if (user?.role === 'SPONSOR') {
      return true;
    }
    return router.createUrlTree(['/']);
  });
};
