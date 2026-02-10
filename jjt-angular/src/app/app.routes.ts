import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'children',
    loadComponent: () => import('./pages/children/children.component').then(m => m.ChildrenComponent),
    canActivate: [authGuard]
  },
  {
    path: 'children/:childId',
    loadComponent: () => import('./pages/child-detail/child-detail.component').then(m => m.ChildDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'children/:childId/sponsor',
    loadComponent: () => import('./pages/sponsor-commit/sponsor-commit.component').then(m => m.SponsorCommitComponent),
    canActivate: [authGuard]
  },
  {
    path: 'sponsor/confirmation',
    loadComponent: () => import('./pages/sponsor-confirmation/sponsor-confirmation.component').then(m => m.SponsorConfirmationComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent),
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'JJT_ADMIN', 'ORG_ADMIN'] }
  },
  {
    path: '**',
    redirectTo: ''
  }
];
