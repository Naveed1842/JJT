import { CanActivateChildFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { RoleService } from '../../services/role.service';

export const adminAuthGuard: CanActivateChildFn = (_route, state) => {
  const roleService = inject(RoleService);
  const router = inject(Router);

  if (!roleService.hasStoredRole()) {
    router.navigate(['/admin/login'], { queryParams: { redirect: state.url } });
    return false;
  }

  return true;
};
