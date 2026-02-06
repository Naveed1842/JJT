import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const roleInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/public/')) {
    return next(req);
  }
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
};
