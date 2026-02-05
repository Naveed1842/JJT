import { HttpInterceptorFn } from '@angular/common/http';

export const roleInterceptor: HttpInterceptorFn = (req, next) => {
  // Get role from localStorage or default to 'ORG_ADMIN'
  const role = localStorage.getItem('userRole') || 'ORG_ADMIN';
  
  // Clone the request and add the X-ROLE header
  const clonedRequest = req.clone({
    setHeaders: {
      'X-ROLE': role
    }
  });
  
  return next(clonedRequest);
};
