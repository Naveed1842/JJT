import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'children',
    loadComponent: () =>
      import('./pages/one-child-at-a-time/one-child-at-a-time.component').then(
        m => m.OneChildAtATimeComponent
      )
  },
  {
    path: 'children/:childId',
    loadComponent: () => import('./pages/child-detail/child-detail.component').then(m => m.ChildDetailComponent)
  },
  {
    path: 'children/:childId/sponsor',
    loadComponent: () => import('./pages/sponsor-commit/sponsor-commit.component').then(m => m.SponsorCommitComponent)
  },
  {
    path: 'sponsor/confirmation',
    loadComponent: () => import('./pages/sponsor-confirmation/sponsor-confirmation.component').then(m => m.SponsorConfirmationComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
